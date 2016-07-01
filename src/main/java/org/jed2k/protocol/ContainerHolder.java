package org.jed2k.protocol;

import java.nio.ByteBuffer;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.jed2k.exception.JED2KException;

import static org.jed2k.protocol.Unsigned.uint32;
import static org.jed2k.protocol.Unsigned.uint16;
import static org.jed2k.protocol.Unsigned.uint8;

/**
 *
 * @param <CS>
 * @param <Elem>
 *
 * TODO - remove this class
 */
public class ContainerHolder<CS extends UNumber, Elem extends Serializable>
        extends AbstractCollection<Elem> implements Serializable {

    private final CS size;
    public final Collection<Elem> collection;
    private final Class<Elem> clazz;
    
    public static <T extends Serializable> ContainerHolder<UInt8, T> make8(
            Collection<T> template, Class<T> clazz) {
        return new ContainerHolder<UInt8, T>(uint8(template.size()), template,
                clazz);
    }

    public static <T extends Serializable> ContainerHolder<UInt16, T> make16(
            Collection<T> template, Class<T> clazz) {
        return new ContainerHolder<UInt16, T>(uint16(template.size()),
                template, clazz);
    }

    public static <T extends Serializable> ContainerHolder<UInt32, T> make32(
            Collection<T> template, Class<T> clazz) {
        return new ContainerHolder<UInt32, T>(uint32(template.size()),
                template, clazz);
    }

    public static <T extends Serializable> ContainerHolder<UInt8, T> list8(Class<T> clazz) {
        return new ContainerHolder<UInt8, T>(uint8(0), new LinkedList<T>(), clazz);
    }

    public ContainerHolder(CS s, Class<Elem> clazz) {
        this.size = s;
        this.clazz = clazz;
        this.collection = null;
    }
    
    public ContainerHolder(CS size_factor, Collection<Elem> collection,
            Class<Elem> clazz) {
        this.size = size_factor;
        this.collection = collection;
        this.clazz = clazz;
    }

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        size.get(src);

        try {
            for (int i = 0; i < size.intValue(); ++i) {
                Elem e = clazz.newInstance();
                e.get(src);
                collection.add(e);
            }
        } catch (InstantiationException e) {
            assert (false);
        } catch (IllegalAccessException e1) {
            assert (false);
        }

        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        size.assign(collection.size());
        size.put(dst);
        Iterator<Elem> itr = collection.iterator();
        while (itr.hasNext()) {
            Elem e = itr.next();
            e.put(dst);
        }
        return dst;
    }

    @Override
    public int bytesCount() {
        int sz = 0;
        Iterator<Elem> itr = collection.iterator();
        while (itr.hasNext()) {
            sz += itr.next().bytesCount();
        }

        return sz + size.bytesCount();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(size.toString());
        sb.append(" ");
        Iterator<Elem> itr = collection.iterator();
        while (itr.hasNext()) {
            sb.append(itr.next()).append(" ");
        }

        return sb.toString();
    }

    @Override
    public Iterator<Elem> iterator() {
        return collection.iterator();
    }

    @Override
    public boolean add(Elem e) {
        if (collection.add(e)) {
            size.assign(size.intValue()+1);
            return true;
        }
        
        return false;
    }

    @Override
    public int size() {
        return collection.size();
    }

}