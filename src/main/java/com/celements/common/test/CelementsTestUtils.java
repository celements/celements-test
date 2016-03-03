package com.celements.common.test;

import static org.easymock.EasyMock.*;

import java.util.Map;

import org.easymock.IAnswer;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.PropertyClass;

public class CelementsTestUtils {

  public static BaseClass expectNewBaseObject(AbstractBridgedComponentTestCase testCase,
      final DocumentReference classRef) throws XWikiException {
    BaseClass bClass = createBaseClassMock(testCase, classRef);
    expect(bClass.newCustomClassInstance(same(testCase.getContext()))).andAnswer(
      new IAnswer<BaseObject>() {
        @Override
        public BaseObject answer() throws Throwable {
          BaseObject bObj = new BaseObject();
          bObj.setXClassReference(classRef);
          return bObj;
        }
      }).anyTimes();
    return bClass;
  }
  
  public static  BaseClass expectPropertyClass(AbstractBridgedComponentTestCase testCase,
      DocumentReference classRef, String fieldName, PropertyClass propClass
      ) throws XWikiException {
    BaseClass bClass = createBaseClassMock(testCase, classRef);
    expectPropertyClass(bClass, fieldName, propClass);
    return bClass;
  }
  
  public static  BaseClass expectPropertyClass(BaseClass bClass, String fieldName,
      PropertyClass propClass) {
    expect(bClass.get(eq(fieldName))).andReturn(propClass).anyTimes();
    return bClass;
  }
  
  public static  BaseClass expectPropertyClasses(AbstractBridgedComponentTestCase testCase,
      DocumentReference classRef, Map<String, PropertyClass> fieldMap
      ) throws XWikiException {
    BaseClass bClass = createBaseClassMock(testCase, classRef);
    expectPropertyClasses(bClass, fieldMap);
    return bClass;
  }
  
  public static  BaseClass expectPropertyClasses(BaseClass bClass,
      Map<String, PropertyClass> fieldMap) {
    for (String fieldName : fieldMap.keySet()) {
      expectPropertyClass(bClass, fieldName, fieldMap.get(fieldName));
    }
    return bClass;
  }

  public static  BaseClass createBaseClassMock(AbstractBridgedComponentTestCase testCase,
      DocumentReference classRef) throws XWikiException {
    BaseClass bClass = testCase.createMockAndAddToDefault(BaseClass.class);
    expect(testCase.getWikiMock().getXClass(eq(classRef), same(testCase.getContext()))
        ).andReturn(bClass).anyTimes();
    expect(bClass.getXClassReference()).andReturn(classRef).anyTimes();
    return bClass;
  }

}
