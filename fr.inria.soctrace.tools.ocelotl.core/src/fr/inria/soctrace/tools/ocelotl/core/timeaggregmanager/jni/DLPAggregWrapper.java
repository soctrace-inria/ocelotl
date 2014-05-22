/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.8
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.jni;

public class DLPAggregWrapper {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected DLPAggregWrapper(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(DLPAggregWrapper obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        lpaggregJNI.delete_DLPAggregWrapper(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public DLPAggregWrapper(int dimension) {
    this(lpaggregJNI.new_DLPAggregWrapper(dimension), true);
  }

  public int newLeaf(int parent, int id) {
    return lpaggregJNI.DLPAggregWrapper_newLeaf(swigCPtr, this, parent, id);
  }

  public int newNode(int parent, int id) {
    return lpaggregJNI.DLPAggregWrapper_newNode(swigCPtr, this, parent, id);
  }

  public int newRoot(int id) {
    return lpaggregJNI.DLPAggregWrapper_newRoot(swigCPtr, this, id);
  }

  public void validate() {
    lpaggregJNI.DLPAggregWrapper_validate(swigCPtr, this);
  }

  public boolean hasFullAggregation(int id) {
    return lpaggregJNI.DLPAggregWrapper_hasFullAggregation(swigCPtr, this, id);
  }

  public int getPart(int id, int index) {
    return lpaggregJNI.DLPAggregWrapper_getPart(swigCPtr, this, id, index);
  }

  public int getPartNumber() {
    return lpaggregJNI.DLPAggregWrapper_getPartNumber(swigCPtr, this);
  }

  public float getParameter(int index) {
    return lpaggregJNI.DLPAggregWrapper_getParameter(swigCPtr, this, index);
  }

  public int getParameterNumber() {
    return lpaggregJNI.DLPAggregWrapper_getParameterNumber(swigCPtr, this);
  }

  public double getGainByIndex(int index) {
    return lpaggregJNI.DLPAggregWrapper_getGainByIndex(swigCPtr, this, index);
  }

  public double getGainByParameter(double parameter) {
    return lpaggregJNI.DLPAggregWrapper_getGainByParameter(swigCPtr, this, parameter);
  }

  public double getLossByIndex(int index) {
    return lpaggregJNI.DLPAggregWrapper_getLossByIndex(swigCPtr, this, index);
  }

  public double getLossByParameter(double parameter) {
    return lpaggregJNI.DLPAggregWrapper_getLossByParameter(swigCPtr, this, parameter);
  }

  public void computeQualities(boolean normalization) {
    lpaggregJNI.DLPAggregWrapper_computeQualities(swigCPtr, this, normalization);
  }

  public void computeParts(double parameter) {
    lpaggregJNI.DLPAggregWrapper_computeParts(swigCPtr, this, parameter);
  }

  public void computeDichotomy(float threshold) {
    lpaggregJNI.DLPAggregWrapper_computeDichotomy(swigCPtr, this, threshold);
  }

  public void setValue(int id, int i, double value) {
    lpaggregJNI.DLPAggregWrapper_setValue__SWIG_0(swigCPtr, this, id, i, value);
  }

  public void push_back(int id, double value) {
    lpaggregJNI.DLPAggregWrapper_push_back__SWIG_0(swigCPtr, this, id, value);
  }

  public void addVector(int id) {
    lpaggregJNI.DLPAggregWrapper_addVector(swigCPtr, this, id);
  }

  public void setValue(int id, int i, int j, double value) {
    lpaggregJNI.DLPAggregWrapper_setValue__SWIG_1(swigCPtr, this, id, i, j, value);
  }

  public void push_back(int id, int i, double value) {
    lpaggregJNI.DLPAggregWrapper_push_back__SWIG_1(swigCPtr, this, id, i, value);
  }

}
