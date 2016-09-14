package org.dkf.jed2k.protocol;

import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;

import java.nio.ByteBuffer;
import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by inkpot on 01.07.2016.
 * This class handles variable structures with different header sizes like:
 * itemsCount(N)
 * item0
 * item1
 * ....
 * itemN-1
 */
public class Container<N extends UNumber, E extends Serializable> extends AbstractCollection<E> implements Serializable{
    private final N n;
    private LinkedList<E> collection = null;
    private final Class<E> clazz;

    private LinkedList<E> holder() {
        if (collection == null) collection = new LinkedList<E>();
        return collection;
    }

    public Container(N n, Class<E> clazz) {
        this.n = n;
        this.clazz = clazz;
    }

    public static<S extends UNumber, T extends Serializable> Container<S, T> make(S s, Class<T> clazz) {
        return new Container(s, clazz);
    }

    public static <T extends Serializable> Container<UInt8, T> makeByte(Class<T> clazz) {
        return new Container<UInt8, T>(new UInt8(0), clazz);
    }

    public static <T extends Serializable> Container<UInt16, T> makeShort(Class<T> clazz) {
        return new Container<UInt16, T>(new UInt16(0), clazz);
    }

    public static <T extends Serializable> Container<UInt32, T> makeInt(Class<T> clazz) {
        return new Container<UInt32, T>(new UInt32(0), clazz);
    }

    public void assignFrom(final Iterable<E> origin) {
        for(final E e: origin) {
            holder().add(e);
        }
    }

    @Override
    public Iterator<E> iterator() {
        if (collection == null)
            return new Iterator<E>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public E next() {
                    return null;
                }

                @Override
                public void remove() {
                    throw new RuntimeException("Remove not implemented for empty container");
                }
            };

        return collection.iterator();
    }

    @Override
    public int size() {
        return (collection != null)?collection.size():0;
    }

    @Override
    public boolean add(E e) {
        holder().add(e);
        return true;
    }

    public final E get(int i) {
        assert(i < holder().size());
        return holder().get(i);
    }

    public void remove(E e) {
        if (collection != null) {
            collection.remove(e);
        }
    }

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        n.get(src);

        try {
            for (int i = 0; i < n.intValue(); ++i) {
                E e = clazz.newInstance();
                e.get(src);
                holder().add(e);
            }
        } catch (InstantiationException e) {
            throw new JED2KException(ErrorCode.GENERIC_INSTANTIATION_ERROR);
        } catch (IllegalAccessException e1) {
            throw new JED2KException(ErrorCode.GENERIC_ILLEGAL_ACCESS);
        }

        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        n.assign(size());
        n.put(dst);
        if (!isEmpty()) {
            // access to internal collection only if size > 0 collection is null otherwise
            Iterator<E> itr = collection.iterator();
            while (itr.hasNext()) {
                E e = itr.next();
                e.put(dst);
            }
        }
        return dst;
    }

    @Override
    public int bytesCount() {
        int sz = n.bytesCount();

        if (collection != null) {
            Iterator<E> itr = collection.iterator();
            while (itr.hasNext()) {
                sz += itr.next().bytesCount();
            }
        }

        return sz;
    }
}
