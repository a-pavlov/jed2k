package org.jed2k.disk.test;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.concurrent.*;

import org.jed2k.Constants;
import org.jed2k.AsyncOperationResult;
import org.junit.Test;

public class DiskIOManagerTest {

    private class Transfer {
        public int o1 = 0;
        public int o2 = 0;

        public void onOperation1() {
            o1++;
        }

        public void onOperation2() {
            o2++;
        }
    }

    private class AsyncResult1 implements AsyncOperationResult {
        private Transfer acceptor;

        public AsyncResult1(Transfer t) {
            assert(t != null);
            acceptor = t;
        }

        @Override
        public void onCompleted() {
            acceptor.onOperation1();
        }
    }

    private class AsyncResult2 implements AsyncOperationResult {
        private Transfer acceptor;

        public AsyncResult2(Transfer t) {
            assert(t != null);
            acceptor = t;
        }

        @Override
        public void onCompleted() {
            acceptor.onOperation2();
        }
    }

    private class AsyncTask1 implements Callable<AsyncOperationResult> {
        private Transfer acceptor;

        public AsyncTask1(Transfer t) {
            acceptor = t;
        }

        @Override
        public AsyncOperationResult call() throws Exception {
            TimeUnit.MILLISECONDS.sleep(10);
            return new AsyncResult1(acceptor);
        }
    }

    private class AsyncTask2 implements Callable<AsyncOperationResult> {
        private Transfer acceptor;

        public AsyncTask2(Transfer t) {
            acceptor = t;
        }

        @Override
        public AsyncOperationResult call() throws Exception {
            TimeUnit.MILLISECONDS.sleep(20);
            return new AsyncResult2(acceptor);
        }
    }

    private class AsyncEntry {
        public Transfer origin = null;
        public Future<AsyncOperationResult> future =  null;

        public AsyncEntry(Transfer t, Future<AsyncOperationResult> f) {
            origin = t;
            future = f;
        }

    }

    private LinkedList<AsyncEntry> asyncTasks = new LinkedList<AsyncEntry>();

    @Test
    public void testService() {
        Transfer transfer = new Transfer();
        Transfer transfer2 = new Transfer();
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        asyncTasks.addLast(new AsyncEntry(transfer, executorService.submit(new AsyncTask1(transfer))));
        asyncTasks.addLast(new AsyncEntry(transfer, executorService.submit(new AsyncTask2(transfer))));
        asyncTasks.addLast(new AsyncEntry(transfer, executorService.submit(new AsyncTask2(transfer))));

        asyncTasks.addLast(new AsyncEntry(transfer2, executorService.submit(new AsyncTask1(transfer2))));
        asyncTasks.addLast(new AsyncEntry(transfer2, executorService.submit(new AsyncTask2(transfer2))));

        // cancel transfer 2 tasks
        for(AsyncEntry e: asyncTasks) {
            if (e.origin == transfer2) {
                e.future.cancel(false);
            }
        }

        while(!asyncTasks.isEmpty()) {
            AsyncEntry head = asyncTasks.peekFirst();
            while(head != null) {
                if (!head.future.isDone()) {
                    break;
                }

                try {
                    if (!head.future.isCancelled()) head.future.get().onCompleted();
                } catch(Exception e) {
                    // temporary do nothing
                }

                asyncTasks.removeFirst();
                head = asyncTasks.peekFirst();
            }
        }

        assertEquals(1, transfer.o1);
        assertEquals(2, transfer.o2);
        assertEquals(0, transfer2.o1);
        assertEquals(0, transfer2.o2);
        executorService.shutdown();
    }

    @Test
    public void testFile() throws FileNotFoundException, IOException {
    	/*
        RandomAccessFile f = new RandomAccessFile("f", "rw");
        f.setLength(Constants.PIECE_SIZE);
        FileChannel channel = f.getChannel();
        LinkedList<ByteBuffer> windows = new LinkedList<ByteBuffer>();
        for(int i = 0; i < Constants.BLOCKS_PER_PIECE; ++i) {
            windows.add(channel.map(FileChannel.MapMode.READ_WRITE, i*Constants.BLOCK_SIZE, Constants.BLOCK_SIZE));
        }
        
        int index = 0;
        for(ByteBuffer bb: windows) {
            for(int i = 0; i < Constants.BLOCK_SIZE; ++i) {
                bb.put((byte)index);
            }
            ++index;
        }
        
        windows.clear();
        f.close();
        InputStream istream = new FileInputStream("f");
        for(int i = 0; i < Constants.PIECE_SIZE; ++i) {
            int value = i/(int)Constants.BLOCK_SIZE;
            assertEquals(value, istream.read());
        }
        
        istream.close();
        */
    }
}
