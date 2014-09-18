package org.jed2k.protocol;

import java.util.Collection;
import java.util.Iterator;

public class ContainerHolder<CS extends UNumber, Elem extends Serializable> implements Serializable {
  public CS size;
  public Collection<Elem> collection;
  private Class<Elem> clazz;
  
  public ContainerHolder(CS size_factor, Collection<Elem> collection, Class<Elem> clazz){
      this.size = size_factor;
      this.collection = collection;
      this.clazz = clazz;
  }
  
  @Override
  public Buffer get(Buffer src) {
    size.get(src);
    for(int i = 0; i < size.intValue(); ++i){
        try{
            Elem e = clazz.newInstance();
            e.get(src);
            collection.add(e);
        }catch(Exception e){
            
        }
    }
    return src;
  }

  @Override
  public Buffer put(Buffer dst) {
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
  
  public final int size(){
    return size.intValue();
  }
  
  public final int sizeCollection(){
    return collection.size();
  }  
  
}