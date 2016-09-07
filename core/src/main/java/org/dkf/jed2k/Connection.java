package org.dkf.jed2k;

import org.dkf.jed2k.exception.BaseErrorCode;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;

public abstract class Connection implements Dispatcher {
    private final Logger log = LoggerFactory.getLogger(ServerConnection.class);

    /**
     * NIO infrastructure
     */
    SocketChannel socket;
    SelectionKey key = null;

    /**
     * buffer for incoming packet's body
     */
    private ByteBuffer bufferIncoming;

    /**
     * buffer for outgoing packets, can contain more than one packet at the same time
     */
    private ByteBuffer bufferOutgoing;

    /**
     * order for packets to send to remote peer
     */
    private LinkedList<Serializable> outgoingOrder = new LinkedList<Serializable>();

    /**
     * outgoing order has pending packets or prewious write operation still in progress
     */
    private boolean writeInProgress = false;

    /**
     * packets parser object
     */
    private final PacketCombiner packetCombainer;

    /**
     * origin
     */
    final Session session;

    /**
     * last received packet's header
     */
    protected PacketHeader header = new PacketHeader();

    /**
     * small buffer for processing packet's headers only
     */
    private ByteBuffer headerBuffer = ByteBuffer.allocate(PacketHeader.SIZE);
    private Statistics stat = new Statistics();
    long lastReceive = Time.currentTime();
    private boolean disconnecting = false;

    protected Connection(ByteBuffer bufferIncoming,
            ByteBuffer bufferOutgoing,
            PacketCombiner packetCombiner,
            Session session) throws IOException {
        this.bufferIncoming = bufferIncoming;
        this.bufferOutgoing = bufferOutgoing;
        this.bufferIncoming.order(ByteOrder.LITTLE_ENDIAN);
        this.bufferOutgoing.order(ByteOrder.LITTLE_ENDIAN);
        this.headerBuffer.order(ByteOrder.LITTLE_ENDIAN);
        this.packetCombainer = packetCombiner;
        this.session = session;
        socket = SocketChannel.open();
        socket.configureBlocking(false);
        key = socket.register(session.selector, SelectionKey.OP_CONNECT, this);
    }

    protected Connection(ByteBuffer bufferIncoming,
            ByteBuffer bufferOutgoing,
            PacketCombiner packetCombiner,
            Session session, SocketChannel socket) throws IOException {
        this.bufferIncoming = bufferIncoming;
        this.bufferOutgoing = bufferOutgoing;
        this.bufferIncoming.order(ByteOrder.LITTLE_ENDIAN);
        this.bufferOutgoing.order(ByteOrder.LITTLE_ENDIAN);
        this.headerBuffer.order(ByteOrder.LITTLE_ENDIAN);
        this.packetCombainer = packetCombiner;
        this.session = session;
        if (socket != null) {
            this.socket = socket;
            this.socket.configureBlocking(false);
            key = socket.register(session.selector, SelectionKey.OP_READ, this);
        }
    }

    public void onConnectable() {
        try {
            socket.finishConnect();
            onConnect();
            lastReceive = Time.currentTime();
        } catch(IOException e) {
            log.error(e.getMessage());
            close(ErrorCode.IO_EXCEPTION);
        } catch(JED2KException e) {
            log.error(e.getMessage());
            close(e.getErrorCode());
        } catch(Exception e) {
            close(ErrorCode.NOT_CONNECTED);
        }
    }

    protected void processHeader() throws JED2KException {
        assert(!header.isDefined());
        assert(headerBuffer.remaining() != 0);

        int bytes;
        try {
            bytes = socket.read(headerBuffer);
        }
        catch(NotYetConnectedException e) {
            throw new JED2KException(ErrorCode.NOT_CONNECTED);
        }
        catch(IOException e) {
            throw new JED2KException(ErrorCode.IO_EXCEPTION);
        }

        if (bytes == -1) throw new JED2KException(ErrorCode.END_OF_STREAM);

        if (headerBuffer.remaining() == 0) {
            headerBuffer.flip();
            assert(headerBuffer.remaining() == PacketHeader.SIZE);
            header.get(headerBuffer);
            headerBuffer.clear();
            int serviceSize = packetCombainer.serviceSize(header);
            validatePacketSize(serviceSize);

            // re-create buffer temp code
            if (bufferIncoming.capacity() < serviceSize) {
                log.debug("re-create incoming buffer to {}", serviceSize);
                bufferIncoming = ByteBuffer.allocate(serviceSize);
                bufferIncoming.order(ByteOrder.LITTLE_ENDIAN);
            }

            bufferIncoming.limit(packetCombainer.serviceSize(header));
            log.trace("processHeader: {} await bytes: {}", header.toString(), packetCombainer.serviceSize(header));
            stat.receiveBytes(PacketHeader.SIZE, 0);
        }
    }

    /**
     * override this method if you need special behaviour in peer or server connection
     * @throws JED2KException
     */
    void validatePacketSize(int packetSize) throws JED2KException {
        if (packetSize < 0) throw new JED2KException(ErrorCode.PACKET_SIZE_INCORRECT);
        if (packetSize > Constants.BLOCK_SIZE_INT) throw new JED2KException(ErrorCode.PACKET_SIZE_OVERFLOW);
    }

    protected void processBody() throws JED2KException {
        if(bufferIncoming.remaining() != 0) {
            int bytes;

            try {
                bytes = socket.read(bufferIncoming);
            }
            catch(NotYetConnectedException e) {
                throw new JED2KException(ErrorCode.NOT_CONNECTED);
            }
            catch(IOException e) {
                throw new JED2KException(ErrorCode.IO_EXCEPTION);
            }

            if (bytes == -1) throw new JED2KException(ErrorCode.END_OF_STREAM);
        }

        if (bufferIncoming.remaining() == 0) {
            bufferIncoming.flip();
            stat.receiveBytes(bufferIncoming.remaining(), 0);
            Serializable packet = packetCombainer.unpack(header, bufferIncoming);
            bufferIncoming.clear();
            if (packet != null && (packet instanceof Dispatchable)) {
                ((Dispatchable)packet).dispatch(this);
            } else {
                log.warn("last packet null or is not Dispatchable!");
            }
            header.reset();
        }
    }

    void onReadable() {
        try {
            if (!header.isDefined()) {
                processHeader();
                // if body dataSize for packet is zero - start process body explicitly now
                if (bufferIncoming.remaining() == 0) {
                    processBody();
                }
            } else {
                processBody();
            }

            lastReceive = Time.currentTime();
        } catch(JED2KException e) {
            log.error(e.toString());
            close(e.getErrorCode());
        }
    }

    void onWriteable() {
        try {
            bufferOutgoing.clear();
            /**
             * previous buffer was completely sent to socket
             * obtain new write in progress status
             */
            writeInProgress = !outgoingOrder.isEmpty();
            Iterator<Serializable> itr = outgoingOrder.iterator();
            while(itr.hasNext()) {
            	// try to serialize packet into buffer
                Serializable s = itr.next();
                if (!packetCombainer.pack(s, bufferOutgoing)) break;
                log.trace("{} >> {}", s.toString(), getEndpoint());
                itr.remove();
            }

            // if write in progress we have to have at least one packet in outgoing buffer
            // check write not in progress or outgoing buffer position not in begin of buffer
            assert(!writeInProgress || bufferOutgoing.position() != 0);

            /**
             * if write in progress send current all data in buffer to remote peer and wait writeable again
             */
            if (writeInProgress) {
                bufferOutgoing.flip();
                stat.sendBytes(bufferOutgoing.remaining(), 0);
                socket.write(bufferOutgoing);
            } else {
                // write not in progress - wait for data from remote peer
                key.interestOps(SelectionKey.OP_READ);
            }
        }
        catch(JED2KException e) {
            log.error(e.getMessage());
            close(e.getErrorCode());
        }
        catch(NotYetConnectedException e) {
            log.error(e.getMessage());
            close(ErrorCode.NOT_CONNECTED);
        }
        catch (IOException e) {
            log.error(e.getMessage());
            close(ErrorCode.IO_EXCEPTION);
        }
    }

    void doRead() {
        key.interestOps(SelectionKey.OP_READ);
    }

    protected abstract void onConnect() throws JED2KException;
    protected abstract void onDisconnect(BaseErrorCode ec);

    void connect(final InetSocketAddress address) throws JED2KException {
        try {
            socket.connect(address);
        } catch(IOException e) {
           log.error(e.getMessage());
           close(ErrorCode.IO_EXCEPTION);
        }
    }

    void close(BaseErrorCode ec) {
        log.debug("{} close connection {}", getEndpoint(), ec);
        try {
            socket.close();
        } catch(IOException e) {
            log.error(e.getMessage());
        }
        catch(Exception e) {
            log.error(e.getMessage());
        }
        finally {
            key.cancel();
            disconnecting = true;
            onDisconnect(ec);
        }
    }

    void write(Serializable packet) {
        outgoingOrder.add(packet);
        if (!writeInProgress) {
            key.interestOps(SelectionKey.OP_WRITE);
            onWriteable();
        }
    }

    void secondTick(long currentSessionTime) {
        stat.secondTick(currentSessionTime);
    }

    public Statistics statistics() {
        return stat;
    }

    final boolean isDisconnecting() {
        return disconnecting;
    }

    abstract NetworkIdentifier getEndpoint();
}
