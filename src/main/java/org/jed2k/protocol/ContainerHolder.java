package org.jed2k.protocol;

import java.util.Collection;
import java.util.Iterator;

public class ContainerHolder<CS extends UNumber, Elem extends Serializable> implements Serializable {
  private CS size;
  private Collection<Elem> collection;
  
  @Override
  public Buffer get(Buffer src) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Buffer put(Buffer dst) {
    // TODO Auto-generated method stub
    dst.put(size);
    Iterator<Elem> itr = collection.iterator();
    while(itr.hasNext()){
      Elem e = itr.next();
      dst.put(e);
    }    
    return dst;
  }
  
}