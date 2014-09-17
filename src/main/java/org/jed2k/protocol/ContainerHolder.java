package org.jed2k.protocol;

import java.util.Collection;
import java.util.Iterator;

public class ContainerHolder<CS extends UNumber, Elem extends Serializable> implements Serializable {
  private CS size;
  private Collection<Elem> collection;
  private Class<Elem> clazz;
  
  ContainerHolder(CS size_factor, Collection<Elem> collection, Class<Elem> clazz){
      size = size_factor;
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
    size.put(dst);
    Iterator<Elem> itr = collection.iterator();
    while(itr.hasNext()){
      Elem e = itr.next();
      e.put(dst);      
    }
    return dst;
  }
  
}