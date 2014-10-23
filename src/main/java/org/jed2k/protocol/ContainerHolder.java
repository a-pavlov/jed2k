package org.jed2k.protocol;

import java.nio.ByteBuffer;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;

import org.jed2k.exception.JED2KException;

import static org.jed2k.protocol.Unsigned.uint32;
import static org.jed2k.protocol.Unsigned.uint16;
import static org.jed2k.protocol.Unsigned.uint8;

public class ContainerHolder<CS extends UNumber, Elem extends Serializable> extends AbstractCollection<Elem> implements Serializable {
      private static Logger log = Logger.getLogger(ContainerHolder.class.getName());
      
      public final CS size;
      public final Collection<Elem> collection;
      private final Class<Elem> clazz;
      
      public ContainerHolder(CS size_factor, Collection<Elem> collection, Class<Elem> clazz) {
          this.size = size_factor;
          this.collection = collection;
          this.clazz = clazz;
      }
      
      public static <T extends Serializable> ContainerHolder<UInt8, T> make8(Collection<T> template, Class<T> clazz) {
          return new ContainerHolder<UInt8, T>(uint8(template.size()), template, clazz);
      }
      
      public static <T extends Serializable> ContainerHolder<UInt16, T> make16(Collection<T> template, Class<T> clazz) {
          return new ContainerHolder<UInt16, T>(uint16(template.size()), template, clazz);
      }
      
      public static <T extends Serializable> ContainerHolder<UInt32, T> make32(Collection<T> template, Class<T> clazz) {
          return new ContainerHolder<UInt32, T>(uint32(template.size()), template, clazz);
      }
      
      @Override
      public ByteBuffer get(ByteBuffer src) throws JED2KException {
        size.get(src);
        
        try {
            for(int i = 0; i < size.intValue(); ++i) {            
                Elem e = clazz.newInstance();
                e.get(src);
                collection.add(e);
            }
        } catch(InstantiationException e) {
            assert(false);            
        } catch (IllegalAccessException e1) {
            assert(false);
        }
        
        return src;
      }
    
      @Override
      public ByteBuffer put(ByteBuffer dst)  throws JED2KException {
        size.assign(collection.size());
        size.put(dst);
        Iterator<Elem> itr = collection.iterator();
        while(itr.hasNext()){
          Elem e = itr.next();
          e.put(dst);      
        }
        return dst;
      }
      
      public boolean isConsistent(){
          return size.intValue() == collection.size();
      }
      
      public final int count(){
        return size.intValue();
      }
      
      public final int sizeCollection(){
        return collection.size();
      }
      
      public Collection<Elem> collection() {
          return collection;
      }

    @Override
    public int size() {
        int sz = 0;
        Iterator<Elem> itr = collection.iterator();
        while(itr.hasNext()){
          sz += itr.next().size();
        }
        
        return sz + size.size();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(size.toString());
        sb.append(" ");
        Iterator<Elem> itr = collection.iterator();
        while(itr.hasNext()) {
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
        return collection.add(e);
    }
  
}