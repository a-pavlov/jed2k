package org.jed2k.protocol;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Iterator;

public class ContainerHolder<CS extends UNumber, Elem extends Serializable> implements Serializable {
      public CS size;
      public Collection<Elem> collection;
      private Class<Elem> clazz;
      
      public ContainerHolder(CS size_factor, Collection<Elem> collection, Class<Elem> clazz) {
          this.size = size_factor;
          this.collection = collection;
          this.clazz = clazz;
      }
      
      public void add(Elem e) {
          collection.add(e);
      }
      
      @Override
      public ByteBuffer get(ByteBuffer src) throws ProtocolException {
        size.get(src);
        try {
            for(int i = 0; i < size.intValue(); ++i) {            
                Elem e = clazz.newInstance();
                e.get(src);
                collection.add(e);
            }
        } catch(InstantiationException e) {
            throw new ProtocolException(e);
        } catch (IllegalAccessException e1) {
            throw new ProtocolException(e1);
        }
        return src;
      }
    
      @Override
      public ByteBuffer put(ByteBuffer dst)  throws ProtocolException {
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
  
}