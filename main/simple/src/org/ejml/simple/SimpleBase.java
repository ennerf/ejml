/*
 * Copyright (c) 2009-2017, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ejml.simple;

import org.ejml.UtilEjml;
import org.ejml.data.*;
import org.ejml.dense.row.*;
import org.ejml.dense.row.mult.VectorVectorMult_R32;
import org.ejml.dense.row.mult.VectorVectorMult_R64;
import org.ejml.ops.MatrixIO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;


/**
 * Parent of {@link SimpleMatrix} implements all the standard matrix operations and uses
 * generics to allow the returned matrix type to be changed.  This class should be extended
 * instead of SimpleMatrix.
 *
 * @author Peter Abeles
 */
@SuppressWarnings({"unchecked"})
public abstract class SimpleBase <T extends SimpleBase> implements Serializable {

    static final long serialVersionUID = 2342556642L;

    /**
     * Internal matrix which this is a wrapper around.
     */
    protected Matrix mat;

    public SimpleBase( int numRows , int numCols ) {
        mat = new DMatrixRow_F64(numRows, numCols);
    }

    protected SimpleBase() {
    }

    /**
     * Used internally for creating new instances of SimpleMatrix.  If SimpleMatrix is extended
     * by another class this function should be overridden so that the returned matrices are
     * of the correct type.
     *
     * @param numRows number of rows in the new matrix.
     * @param numCols number of columns in the new matrix.
     * @return A new matrix.
     */
    protected abstract T createMatrix( int numRows , int numCols );

    /**
     * <p>
     * Returns a reference to the matrix that it uses internally.  This is useful
     * when an operation is needed that is not provided by this class.
     * </p>
     *
     * @return Reference to the internal DMatrixRow_F64.
     */
    public <T extends Matrix>T getMatrix() {
        return (T)mat;
    }

    public DMatrixRow_F64 matrix_F64() {
        return (DMatrixRow_F64)mat;
    }

    public DMatrixRow_F32 matrix_F32() {
        return (DMatrixRow_F32)mat;
    }


    /**
     * <p>
     * Returns the transpose of this matrix.<br>
     * a<sup>T</sup>
     * </p>
     *
     * @see CommonOps_R64#transpose(DMatrixRow_F64, DMatrixRow_F64)
     *
     * @return A matrix that is n by m.
     */
    public T transpose() {
        T ret = createMatrix(mat.getNumCols(),mat.getNumRows());

        if( bits() == 64 )
            CommonOps_R64.transpose((DMatrixRow_F64)mat,(DMatrixRow_F64)ret.getMatrix());
        else
            CommonOps_R32.transpose((DMatrixRow_F32)mat,(DMatrixRow_F32)ret.getMatrix());

        return ret;
    }

    /**
     * <p>
     * Returns a matrix which is the result of matrix multiplication:<br>
     * <br>
     * c = a * b <br>
     * <br>
     * where c is the returned matrix, a is this matrix, and b is the passed in matrix.
     * </p>
     *
     * @see CommonOps_R64#mult(D1MatrixRow_F64, D1MatrixRow_F64, D1MatrixRow_F64)
     *
     * @param b A matrix that is n by bn. Not modified.
     *
     * @return The results of this operation.
     */
    public T mult( T b ) {
        T ret = createMatrix(mat.getNumRows(),b.getMatrix().getNumCols());

        if( bits() == 64 )
            CommonOps_R64.mult((DMatrixRow_F64)mat,(DMatrixRow_F64)b.getMatrix(),(DMatrixRow_F64)ret.getMatrix());
        else
            CommonOps_R32.mult((DMatrixRow_F32)mat,(DMatrixRow_F32)b.getMatrix(),(DMatrixRow_F32)ret.getMatrix());

        return ret;
    }

    /**
     * <p>
     * Computes the Kronecker product between this matrix and the provided B matrix:<br>
     * <br>
     * C = kron(A,B)
     * </p>

     * @see CommonOps_R64#kron(DMatrixRow_F64, DMatrixRow_F64, DMatrixRow_F64)
     *
     * @param B The right matrix in the operation. Not modified.
     * @return Kronecker product between this matrix and B.
     */
    public T kron( T B ) {
        T ret = createMatrix(mat.getNumRows()*B.numRows(),mat.getNumCols()*B.numCols());

        if( bits() == 64 )
            CommonOps_R64.kron((DMatrixRow_F64)mat,(DMatrixRow_F64)B.getMatrix(),(DMatrixRow_F64)ret.getMatrix());
        else
            CommonOps_R32.kron((DMatrixRow_F32)mat,(DMatrixRow_F32)B.getMatrix(),(DMatrixRow_F32)ret.getMatrix());

        return ret;
    }

    /**
     * <p>
     * Returns the result of matrix addition:<br>
     * <br>
     * c = a + b <br>
     * <br>
     * where c is the returned matrix, a is this matrix, and b is the passed in matrix.
     * </p>
     *
     * @see CommonOps_R64#mult(D1MatrixRow_F64, D1MatrixRow_F64, D1MatrixRow_F64)
     *
     * @param b m by n matrix. Not modified.
     *
     * @return The results of this operation.
     */
    public T plus( T b ) {
        T ret = copy();

        if( bits() == 64 )
            CommonOps_R64.addEquals((DMatrixRow_F64)ret.getMatrix(),(DMatrixRow_F64)b.getMatrix());
        else
            CommonOps_R32.addEquals((DMatrixRow_F32)ret.getMatrix(),(DMatrixRow_F32)b.getMatrix());

        return ret;
    }

    /**
     * <p>
     * Returns the result of matrix subtraction:<br>
     * <br>
     * c = a - b <br>
     * <br>
     * where c is the returned matrix, a is this matrix, and b is the passed in matrix.
     * </p>
     *
     * @see CommonOps_R64#subtract(D1Matrix_F64, D1Matrix_F64, D1Matrix_F64)
     *
     * @param b m by n matrix. Not modified.
     *
     * @return The results of this operation.
     */
    public T minus( T b ) {
        T ret = copy();

        if( bits() == 64 )
            CommonOps_R64.subtract((DMatrixRow_F64)getMatrix(), (DMatrixRow_F64)b.getMatrix(), (DMatrixRow_F64)ret.getMatrix());
        else
            CommonOps_R32.subtract((DMatrixRow_F32)getMatrix(), (DMatrixRow_F32)b.getMatrix(), (DMatrixRow_F32)ret.getMatrix());

        return ret;
    }

    /**
     * <p>
     * Returns the result of matrix-double subtraction:<br>
     * <br>
     * c = a - b <br>
     * <br>
     * where c is the returned matrix, a is this matrix, and b is the passed in double.
     * </p>
     *
     * @see CommonOps_R64#subtract(D1Matrix_F64, double , D1Matrix_F64)
     *
     * @param b Value subtracted from each element
     *
     * @return The results of this operation.
     */
    public T minus( double b ) {
        T ret = copy();

        if( bits() == 64 )
            CommonOps_R64.subtract((DMatrixRow_F64)getMatrix(), b, (DMatrixRow_F64)ret.getMatrix());
        else
            CommonOps_R32.subtract((DMatrixRow_F32)getMatrix(), (float)b, (DMatrixRow_F32)ret.getMatrix());

        return ret;
    }

    /**
     * <p>
     * Returns the result of scalar addition:<br>
     * <br>
     * c = a + b<br>
     * <br>
     * where c is the returned matrix, a is this matrix, and b is the passed in double.
     * </p>
     *
     * @see CommonOps_R64#add( D1Matrix_F64, double , D1Matrix_F64)
     *
     * @param b Value added to each element
     *
     * @return A matrix that contains the results.
     */
    public T plus( double b ) {
        T ret = createMatrix(numRows(),numCols());

        if( bits() == 64 )
            CommonOps_R64.add((DMatrixRow_F64)getMatrix(), b, (DMatrixRow_F64)ret.getMatrix());
        else
            CommonOps_R32.add((DMatrixRow_F32)getMatrix(), (float)b, (DMatrixRow_F32)ret.getMatrix());

        return ret;
    }

    /**
     * <p>
     * Performs a matrix addition and scale operation.<br>
     * <br>
     * c = a + &beta;*b <br>
     * <br>
     * where c is the returned matrix, a is this matrix, and b is the passed in matrix.
     * </p>
     *
     * @see CommonOps_R64#add( D1Matrix_F64, double , D1Matrix_F64, D1Matrix_F64)
     *
     * @param b m by n matrix. Not modified.
     *
     * @return A matrix that contains the results.
     */
    public T plus( double beta , T b ) {
        T ret = copy();

        if( bits() == 64 )
            CommonOps_R64.addEquals((DMatrixRow_F64)ret.getMatrix(),beta,(DMatrixRow_F64)b.getMatrix());
        else
            CommonOps_R32.addEquals((DMatrixRow_F32)ret.getMatrix(),(float)beta,(DMatrixRow_F32)b.getMatrix());

        return ret;
    }

    /**
     * Computes the dot product (a.k.a. inner product) between this vector and vector 'v'.
     *
     * @param v The second vector in the dot product.  Not modified.
     * @return dot product
     */
    public double dot( T v ) {
        if( !isVector() ) {
            throw new IllegalArgumentException("'this' matrix is not a vector.");
        } else if( !v.isVector() ) {
            throw new IllegalArgumentException("'v' matrix is not a vector.");
        }

        if( bits() == 64 )
            return VectorVectorMult_R64.innerProd((DMatrixRow_F64)mat,(DMatrixRow_F64)v.getMatrix());
        else
            return VectorVectorMult_R32.innerProd((DMatrixRow_F32)mat,(DMatrixRow_F32)v.getMatrix());
    }

    /**
     * Returns true if this matrix is a vector.  A vector is defined as a matrix
     * that has either one row or column.
     *
     * @return Returns true for vectors and false otherwise.
     */
    public boolean isVector() {
        return mat.getNumRows() == 1 || mat.getNumCols() == 1;
    }

    /**
     * <p>
     * Returns the result of scaling each element by 'val':<br>
     * b<sub>i,j</sub> = val*a<sub>i,j</sub>
     * </p>
     *
     * @see CommonOps_R64#scale(double, D1Matrix_F64)
     *
     * @param val The multiplication factor.
     * @return The scaled matrix.
     */
    public T scale( double val ) {
        T ret = copy();

        if( bits() == 64 )
            CommonOps_R64.scale(val,(DMatrixRow_F64)ret.getMatrix());
        else
            CommonOps_R32.scale((float)val,(DMatrixRow_F32)ret.getMatrix());

        return ret;
    }

    /**
     * <p>
     * Returns the result of dividing each element by 'val':
     * b<sub>i,j</sub> = a<sub>i,j</sub>/val
     * </p>
     *
     * @see CommonOps_R64#divide(D1Matrix_F64,double)
     *
     * @param val Divisor.
     * @return Matrix with its elements divided by the specified value.
     */
    public T divide( double val ) {
        T ret = copy();

        if( bits() == 64 )
            CommonOps_R64.divide((DMatrixRow_F64)ret.getMatrix(),val);
        else
            CommonOps_R32.divide((DMatrixRow_F32)ret.getMatrix(),(float)val);

        return ret;
    }

    /**
     * <p>
     * Returns the inverse of this matrix.<br>
     * <br>
     * b = a<sup>-1<sup><br>
     * </p>
     *
     * <p>
     * If the matrix could not be inverted then SingularMatrixException is thrown.  Even
     * if no exception is thrown the matrix could still be singular or nearly singular.
     * </p>
     *
     * @see CommonOps_R64#invert(DMatrixRow_F64, DMatrixRow_F64)
     *
     * @throws SingularMatrixException
     *
     * @return The inverse of this matrix.
     */
    public T invert() {
        T ret = createMatrix(mat.getNumRows(), mat.getNumCols());
        if (bits() == 64) {
            if (!CommonOps_R64.invert((DMatrixRow_F64)mat, (DMatrixRow_F64)ret.getMatrix())) {
                throw new SingularMatrixException();
            }
            if (MatrixFeatures_R64.hasUncountable((DMatrixRow_F64)ret.getMatrix()))
                throw new SingularMatrixException("Solution has uncountable numbers");
        } else {
            if (!CommonOps_R32.invert((DMatrixRow_F32)mat, (DMatrixRow_F32)ret.getMatrix())) {
                throw new SingularMatrixException();
            }
            if (MatrixFeatures_R32.hasUncountable((DMatrixRow_F32)ret.getMatrix()))
                throw new SingularMatrixException("Solution has uncountable numbers");
        }
        return ret;
    }

    /**
     * <p>
     * Computes the Moore-Penrose pseudo-inverse
     * </p>
     *
     * @return inverse computed using the pseudo inverse.
     */
    public T pseudoInverse() {
        T ret = createMatrix(mat.getNumCols(),mat.getNumRows());
        if (bits() == 64) {
            CommonOps_R64.pinv((DMatrixRow_F64)mat, (DMatrixRow_F64)ret.getMatrix());
        } else {
            CommonOps_R32.pinv((DMatrixRow_F32)mat, (DMatrixRow_F32)ret.getMatrix());
        }
        return ret;
    }

    /**
     * <p>
     * Solves for X in the following equation:<br>
     * <br>
     * x = a<sup>-1</sup>b<br>
     * <br>
     * where 'a' is this matrix and 'b' is an n by p matrix.
     * </p>
     *
     * <p>
     * If the system could not be solved then SingularMatrixException is thrown.  Even
     * if no exception is thrown 'a' could still be singular or nearly singular.
     * </p>
     *
     * @see CommonOps_R64#solve(DMatrixRow_F64, DMatrixRow_F64, DMatrixRow_F64)
     *
     * @throws SingularMatrixException
     *
     * @param b n by p matrix. Not modified.
     * @return The solution for 'x' that is n by p.
     */
    public T solve( T b )
    {
        T x = createMatrix(mat.getNumCols(),b.getMatrix().getNumCols());

        if (bits() == 64) {
            if (!CommonOps_R64.solve((DMatrixRow_F64)mat, (DMatrixRow_F64)b.getMatrix(), (DMatrixRow_F64)x.getMatrix()))
                throw new SingularMatrixException();

            if (MatrixFeatures_R64.hasUncountable((DMatrixRow_F64)x.getMatrix()))
                throw new SingularMatrixException("Solution contains uncountable numbers");
        } else {
            if (!CommonOps_R32.solve((DMatrixRow_F32)mat, (DMatrixRow_F32)b.getMatrix(), (DMatrixRow_F32)x.getMatrix()))
                throw new SingularMatrixException();

            if (MatrixFeatures_R32.hasUncountable((DMatrixRow_F32)x.getMatrix()))
                throw new SingularMatrixException("Solution contains uncountable numbers");
        }

        return x;
    }


    /**
     * Sets the elements in this matrix to be equal to the elements in the passed in matrix.
     * Both matrix must have the same dimension.
     *
     * @param a The matrix whose value this matrix is being set to.
     */
    public void set( T a ) {
        mat.set(a.getMatrix());
    }


    /**
     * <p>
     * Sets all the elements in this matrix equal to the specified value.<br>
     * <br>
     * a<sub>ij</sub> = val<br>
     * </p>
     *
     * @see CommonOps_R64#fill(D1Matrix_F64, double)
     *
     * @param val The value each element is set to.
     */
    public void set( double val ) {
        if (bits() == 64) {
            CommonOps_R64.fill((DMatrixRow_F64)mat, val);
        } else {
            CommonOps_R32.fill((DMatrixRow_F32)mat, (float)val);
        }
    }

    /**
     * Sets all the elements in the matrix equal to zero.
     *
     * @see CommonOps_R64#fill(D1Matrix_F64, double)
     */
    public void zero() {
        if (bits() == 64) {
            ((DMatrixRow_F64)mat).zero();
        } else {
            ((DMatrixRow_F32)mat).zero();
        }
    }

    /**
     * <p>
     * Computes the Frobenius normal of the matrix:<br>
     * <br>
     * normF = Sqrt{  &sum;<sub>i=1:m</sub> &sum;<sub>j=1:n</sub> { a<sub>ij</sub><sup>2</sup>}   }
     * </p>
     *
     * @see NormOps_R64#normF(D1Matrix_F64)
     *
     * @return The matrix's Frobenius normal.
     */
    public double normF() {
        if (bits() == 64) {
            return NormOps_R64.normF((DMatrixRow_F64)mat);
        } else {
            return NormOps_R32.normF((DMatrixRow_F32)mat);
        }
    }

    /**
     * <p>
     * The condition p = 2 number of a matrix is used to measure the sensitivity of the linear
     * system <b>Ax=b</b>.  A value near one indicates that it is a well conditioned matrix.
     * </p>
     *
     * @see NormOps_R64#conditionP2(DMatrixRow_F64)
     *
     * @return The condition number.
     */
    public double conditionP2() {
        if (bits() == 64) {
            return NormOps_R64.conditionP2((DMatrixRow_F64)mat);
        } else {
            return NormOps_R32.conditionP2((DMatrixRow_F32)mat);
        }
    }

    /**
     * Computes the determinant of the matrix.
     *
     * @see CommonOps_R64#det(DMatrixRow_F64)
     *
     * @return The determinant.
     */
    public double determinant() {
        if (bits() == 64) {
            double ret = CommonOps_R64.det((DMatrixRow_F64)mat);
            // if the decomposition silently failed then the matrix is most likely singular
            if (UtilEjml.isUncountable(ret))
                return 0;
            return ret;
        } else {
            double ret = CommonOps_R32.det((DMatrixRow_F32)mat);
            // if the decomposition silently failed then the matrix is most likely singular
            if (UtilEjml.isUncountable(ret))
                return 0;
            return ret;
        }
    }

    /**
     * <p>
     * Computes the trace of the matrix.
     * </p>
     *
     * @see CommonOps_R64#trace(D1MatrixRow_F64)
     *
     * @return The trace of the matrix.
     */
    public double trace() {
        if (bits() == 64) {
            return CommonOps_R64.trace((DMatrixRow_F64)mat);
        } else {
            return CommonOps_R32.trace((DMatrixRow_F32)mat);
        }
    }

    /**
     * <p>
     * Reshapes the matrix to the specified number of rows and columns.  If the total number of elements
     * is <= number of elements it had before the data is saved.  Otherwise a new internal array is
     * declared and the old data lost.
     * </p>
     *
     * <p>
     * This is equivalent to calling A.getMatrix().reshape(numRows,numCols,false).
     * </p>
     *
     * @see DMatrixRow_F64#reshape(int,int,boolean)
     *
     * @param numRows The new number of rows in the matrix.
     * @param numCols The new number of columns in the matrix.
     */
    public void reshape( int numRows , int numCols ) {
        if (bits() == 64) {
            ((DMatrixRow_F64)mat).reshape(numRows, numCols, false);
        } else {
            ((DMatrixRow_F32)mat).reshape(numRows, numCols, false);
        }
    }

    /**
     * Assigns the element in the Matrix to the specified value.  Performs a bounds check to make sure
     * the requested element is part of the matrix.
     *
     * @param row The row of the element.
     * @param col The column of the element.
     * @param value The element's new value.
     */
    public void set( int row , int col , double value ) {
        if (bits() == 64) {
            ((DMatrixRow_F64)mat).set(row, col, value);
        } else {
            ((DMatrixRow_F32)mat).set(row, col, (float)value);
        }
    }

    /**
     * Assigns an element a value based on its index in the internal array..
     *
     * @param index The matrix element that is being assigned a value.
     * @param value The element's new value.
     */
    public void set( int index , double value ) {
        if (bits() == 64) {
            ((DMatrixRow_F64)mat).set(index, value);
        } else {
            ((DMatrixRow_F32)mat).set(index, (float)value);
        }
    }

    /**
     * <p>
     * Assigns consecutive elements inside a row to the provided array.<br>
     * <br>
     * A(row,offset:(offset + values.length)) = values
     * </p>
     *
     * @param row The row that the array is to be written to.
     * @param offset The initial column that the array is written to.
     * @param values Values which are to be written to the row in a matrix.
     */
    public void setRow( int row , int offset , double ...values ) {
        if (bits() == 64) {
            DMatrixRow_F64 m = (DMatrixRow_F64)mat;
            for (int i = 0; i < values.length; i++) {
                m.set(row, offset + i, values[i]);
            }
        } else {
            DMatrixRow_F32 m = (DMatrixRow_F32)mat;
            for (int i = 0; i < values.length; i++) {
                m.set(row, offset + i, (float)values[i]);
            }
        }
    }

    /**
     * <p>
     * Assigns consecutive elements inside a column to the provided array.<br>
     * <br>
     * A(offset:(offset + values.length),column) = values
     * </p>
     *
     * @param column The column that the array is to be written to.
     * @param offset The initial column that the array is written to.
     * @param values Values which are to be written to the row in a matrix.
     */
    public void setColumn( int column , int offset , double ...values ) {
        if (bits() == 64) {
            DMatrixRow_F64 m = (DMatrixRow_F64)mat;
            for (int i = 0; i < values.length; i++) {
                m.set(offset + i, column, values[i]);
            }
        } else {
            DMatrixRow_F32 m = (DMatrixRow_F32)mat;
            for (int i = 0; i < values.length; i++) {
                m.set(offset + i, column, (float)values[i]);
            }
        }
    }

    /**
     * Returns the value of the specified matrix element.  Performs a bounds check to make sure
     * the requested element is part of the matrix.
     *
     * @param row The row of the element.
     * @param col The column of the element.
     * @return The value of the element.
     */
    public double get( int row , int col ) {
        if (bits() == 64) {
            return ((DMatrixRow_F64)mat).get(row, col);
        } else {
            return ((DMatrixRow_F32)mat).get(row, col);
        }
    }

    /**
     * Returns the value of the matrix at the specified index of the 1D row major array.
     *
     * @see DMatrixRow_F64#get(int)
     *
     * @param index The element's index whose value is to be returned
     * @return The value of the specified element.
     */
    public double get( int index ) {
        if (bits() == 64) {
            return ((DMatrixRow_F64)mat).data[index];
        } else {
            return ((DMatrixRow_F32)mat).data[index];
        }
    }

    /**
     * Returns the index in the matrix's array.
     *
     * @see DMatrixRow_F64#getIndex(int, int)
     *
     * @param row The row number.
     * @param col The column number.
     * @return The index of the specified element.
     */
    public int getIndex( int row , int col ) {
        return row * mat.getNumCols() + col;
    }

    /**
     * Creates a new iterator for traversing through a submatrix inside this matrix.  It can be traversed
     * by row or by column.  Range of elements is inclusive, e.g. minRow = 0 and maxRow = 1 will include rows
     * 0 and 1.  The iteration starts at (minRow,minCol) and ends at (maxRow,maxCol)
     *
     * @param rowMajor true means it will traverse through the submatrix by row first, false by columns.
     * @param minRow first row it will start at.
     * @param minCol first column it will start at.
     * @param maxRow last row it will stop at.
     * @param maxCol last column it will stop at.
     * @return A new MatrixIterator
     */
    public MatrixIterator_F64 iterator(boolean rowMajor, int minRow, int minCol, int maxRow, int maxCol)
    {
        return new MatrixIterator_F64((DMatrixRow_F64)mat,rowMajor, minRow, minCol, maxRow, maxCol);
    }

    /**
     * Creates and returns a matrix which is idential to this one.
     *
     * @return A new identical matrix.
     */
    public T copy() {
        T ret = createMatrix(mat.getNumRows(),mat.getNumCols());
        ret.getMatrix().set(this.getMatrix());
        return ret;
    }

    /**
     * Returns the number of rows in this matrix.
     *
     * @return number of rows.
     */
    public int numRows() {
        return mat.getNumRows();
    }

    /**
     * Returns the number of columns in this matrix.
     *
     * @return number of columns.
     */
    public int numCols() {
        return mat.getNumCols();
    }

    /**
     * Returns the number of elements in this matrix, which is equal to
     * the number of rows times the number of columns.
     *
     * @return The number of elements in the matrix.
     */
    public int getNumElements() {
        if( bits() == 64 )
            return ((DMatrixRow_F64)mat).getNumElements();
        else
            return ((DMatrixRow_F32)mat).getNumElements();
    }


    /**
     * Prints the matrix to standard out.
     */
    public void print() {
        if( bits() == 64 ) {
            MatrixIO.print(System.out, (DMatrixRow_F64)mat);
        } else {
            MatrixIO.print(System.out, (DMatrixRow_F32) mat);
        }
    }

    /**
     * Prints the matrix to standard out with the specified precision.
     */
    public void print(int numChar , int precision) {
        if( bits() == 64 ) {
            MatrixIO.print(System.out, (DMatrixRow_F64)mat, numChar, precision);
        } else {
            MatrixIO.print(System.out, (DMatrixRow_F32)mat, numChar, precision);
        }
    }

    /**
     * <p>
     * Prints the matrix to standard out given a {@link java.io.PrintStream#printf) style floating point format,
     * e.g. print("%f").
     * </p>
     */
    public void print( String format ) {
        if( bits() == 64 ) {
            MatrixIO.print(System.out, (DMatrixRow_F64)mat, format);
        } else {
            MatrixIO.print(System.out, (DMatrixRow_F32)mat, format);
        }
    }

    /**
     * <p>
     * Converts the array into a string format for display purposes.
     * The conversion is done using {@link MatrixIO#print(java.io.PrintStream, Matrix_F64)}.
     * </p>
     *
     * @return String representation of the matrix.
     */
    public String toString() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if( bits() == 64 ) {
            MatrixIO.print(new PrintStream(stream), (DMatrixRow_F64)mat);
        } else {
            MatrixIO.print(new PrintStream(stream), (DMatrixRow_F32)mat);
        }

        return stream.toString();
    }

    /**
     * <p>
     * Creates a new SimpleMatrix which is a submatrix of this matrix.
     * </p>
     * <p>
     * s<sub>i-y0 , j-x0</sub> = o<sub>ij</sub> for all y0 &le; i < y1 and x0 &le; j < x1<br>
     * <br>
     * where 's<sub>ij</sub>' is an element in the submatrix and 'o<sub>ij</sub>' is an element in the
     * original matrix.
     * </p>
     *
     * <p>
     * If any of the inputs are set to T.END then it will be set to the last row
     * or column in the matrix.
     * </p>
     *
     * @param y0 Start row.
     * @param y1 Stop row + 1.
     * @param x0 Start column.
     * @param x1 Stop column + 1.
     * @return The submatrix.
     */
    public T extractMatrix(int y0 , int y1, int x0 , int x1 ) {
        if( y0 == SimpleMatrix.END ) y0 = mat.getNumRows();
        if( y1 == SimpleMatrix.END ) y1 = mat.getNumRows();
        if( x0 == SimpleMatrix.END ) x0 = mat.getNumCols();
        if( x1 == SimpleMatrix.END ) x1 = mat.getNumCols();

        T ret = createMatrix(y1-y0,x1-x0);

        if( bits() == 64 ) {
            CommonOps_R64.extract((DMatrixRow_F64)mat, y0, y1, x0, x1, (DMatrixRow_F64)ret.getMatrix(), 0, 0);
        } else {
            CommonOps_R32.extract((DMatrixRow_F32)mat, y0, y1, x0, x1, (DMatrixRow_F32)ret.getMatrix(), 0, 0);
        }

        return ret;
    }

    /**
     * <p>
     * Extracts a row or column from this matrix. The returned vector will either be a row
     * or column vector depending on the input type.
     * </p>
     *
     * @param extractRow If true a row will be extracted.
     * @param element The row or column the vector is contained in.
     * @return Extracted vector.
     */
    public T extractVector( boolean extractRow , int element )
    {
        int length = extractRow ? mat.getNumCols() : mat.getNumRows();

        T ret = extractRow ? createMatrix(1,length) : createMatrix(length,1);

        if( bits() == 64 ) {
            if (extractRow) {
                SpecializedOps_R64.subvector((DMatrixRow_F64)mat, element, 0, length, true, 0, (DMatrixRow_F64)ret.getMatrix());
            } else {
                SpecializedOps_R64.subvector((DMatrixRow_F64)mat, 0, element, length, false, 0, (DMatrixRow_F64)ret.getMatrix());
            }
        } else {
            if (extractRow) {
                SpecializedOps_R32.subvector((DMatrixRow_F32)mat, element, 0, length, true, 0, (DMatrixRow_F32)ret.getMatrix());
            } else {
                SpecializedOps_R32.subvector((DMatrixRow_F32)mat, 0, element, length, false, 0, (DMatrixRow_F32)ret.getMatrix());
            }
        }

        return ret;
    }

    /**
     * <p>
     * Extracts the diagonal from this matrix and returns them inside a column vector.
     * </p>
     *
     * @see CommonOps_R64#extractDiag(DMatrixRow_F64, DMatrixRow_F64)
     * @return Diagonal elements inside a column vector.
     */
    public T extractDiag()
    {
        int N = Math.min(mat.getNumCols(),mat.getNumRows());

        T diag = createMatrix(N,1);

        if( bits() == 64 ) {
            CommonOps_R64.extractDiag((DMatrixRow_F64)mat, (DMatrixRow_F64)diag.getMatrix());
        } else {
            CommonOps_R32.extractDiag((DMatrixRow_F32)mat, (DMatrixRow_F32)diag.getMatrix());
        }

        return diag;
    }

    /**
     * Checks to see if matrix 'a' is the same as this matrix within the specified
     * tolerance.
     *
     * @param a The matrix it is being compared against.
     * @param tol How similar they must be to be equals.
     * @return If they are equal within tolerance of each other.
     */
    public boolean isIdentical(T a, double tol) {
        if( bits() == 64 ) {
            return MatrixFeatures_R64.isIdentical((DMatrixRow_F64)mat, (DMatrixRow_F64)a.getMatrix(), tol);
        } else {
            return MatrixFeatures_R32.isIdentical((DMatrixRow_F32)mat, (DMatrixRow_F32)a.getMatrix(), (float)tol);
        }
    }

    /**
     * Checks to see if any of the elements in this matrix are either NaN or infinite.
     *
     * @return True of an element is NaN or infinite.  False otherwise.
     */
    public boolean hasUncountable() {
        if( bits() == 64 ) {
            return MatrixFeatures_R64.hasUncountable((DMatrixRow_F64)mat);
        } else {
            return MatrixFeatures_R32.hasUncountable((DMatrixRow_F32)mat);
        }
    }

    /**
     * Computes a full Singular Value Decomposition (SVD) of this matrix with the
     * eigenvalues ordered from largest to smallest.
     *
     * @return SVD
     */
    public SimpleSVD<T> svd() {
        return new SimpleSVD(mat,false);
    }

    /**
     * Computes the SVD in either  compact format or full format.
     *
     * @return SVD of this matrix.
     */
    public SimpleSVD<T> svd( boolean compact ) {
        return new SimpleSVD(mat,compact);
    }

    /**
     * Returns the Eigen Value Decomposition (EVD) of this matrix.
     */
    public SimpleEVD<T> eig() {
        return new SimpleEVD(mat);
    }

    /**
     * Copy matrix B into this matrix at location (insertRow, insertCol).
     *
     * @param insertRow First row the matrix is to be inserted into.
     * @param insertCol First column the matrix is to be inserted into.
     * @param B The matrix that is being inserted.
     */
    public void insertIntoThis(int insertRow, int insertCol, T B) {
        if( bits() == 64 ) {
            CommonOps_R64.insert((DMatrixRow_F64)B.getMatrix(), (DMatrixRow_F64)mat, insertRow, insertCol);
        } else {
            CommonOps_R32.insert((DMatrixRow_F32)B.getMatrix(),(DMatrixRow_F32) mat, insertRow, insertCol);
        }
    }

    /**
     * <p>
     * Creates a new matrix that is a combination of this matrix and matrix B.  B is
     * written into A at the specified location if needed the size of A is increased by
     * growing it.  A is grown by padding the new area with zeros.
     * </p>
     *
     * <p>
     * While useful when adding data to a matrix which will be solved for it is also much
     * less efficient than predeclaring a matrix and inserting data into it.
     * </p>
     *
     * <p>
     * If insertRow or insertCol is set to SimpleMatrix.END then it will be combined
     * at the last row or column respectively.
     * <p>
     *
     * @param insertRow Row where matrix B is written in to.
     * @param insertCol Column where matrix B is written in to.
     * @param B The matrix that is written into A.
     * @return A new combined matrix.
     */
    public T combine( int insertRow, int insertCol, T B) {

        if( insertRow == SimpleMatrix.END ) {
            insertRow = mat.getNumRows();
        }

        if( insertCol == SimpleMatrix.END ) {
            insertCol = mat.getNumCols();
        }

        int maxRow = insertRow + B.numRows();
        int maxCol = insertCol + B.numCols();

        T ret;

        if( maxRow > mat.getNumRows() || maxCol > mat.getNumCols()) {
            int M = Math.max(maxRow,mat.getNumRows());
            int N = Math.max(maxCol,mat.getNumCols());

            ret = createMatrix(M,N);
            ret.insertIntoThis(0,0,this);
        } else {
            ret = copy();
        }

        ret.insertIntoThis(insertRow,insertCol,B);

        return ret;
    }

    /**
     * Returns the maximum absolute value of all the elements in this matrix.  This is
     * equivalent the the infinite p-norm of the matrix.
     *
     * @return Largest absolute value of any element.
     */
    public double elementMaxAbs() {
        if( bits() == 64 ) {
            return CommonOps_R64.elementMaxAbs((DMatrixRow_F64)mat);
        } else {
            return CommonOps_R32.elementMaxAbs((DMatrixRow_F32)mat);
        }
    }

    /**
     * Computes the sum of all the elements in the matrix.
     *
     * @return Sum of all the elements.
     */
    public double elementSum() {
        if( bits() == 64 ) {
            return CommonOps_R64.elementSum((DMatrixRow_F64)mat);
        } else {
            return CommonOps_R32.elementSum((DMatrixRow_F32)mat);
        }
    }

    /**
     * <p>
     * Returns a matrix which is the result of an element by element multiplication of 'this' and 'b':
     * c<sub>i,j</sub> = a<sub>i,j</sub>*b<sub>i,j</sub>
     * </p>
     *
     * @param b A simple matrix.
     * @return The element by element multiplication of 'this' and 'b'.
     */
    public T elementMult( T b )
    {
        T c = createMatrix(mat.getNumRows(),mat.getNumCols());

        if( bits() == 64 ) {
            CommonOps_R64.elementMult((DMatrixRow_F64)mat, (DMatrixRow_F64)b.getMatrix(), (DMatrixRow_F64)c.getMatrix());
        } else {
            CommonOps_R32.elementMult((DMatrixRow_F32)mat, (DMatrixRow_F32)b.getMatrix(), (DMatrixRow_F32)c.getMatrix());
        }

        return c;
    }

    /**
     * <p>
     * Returns a matrix which is the result of an element by element division of 'this' and 'b':
     * c<sub>i,j</sub> = a<sub>i,j</sub>/b<sub>i,j</sub>
     * </p>
     *
     * @param b A simple matrix.
     * @return The element by element division of 'this' and 'b'.
     */
    public T elementDiv( T b )
    {
        T c = createMatrix(mat.getNumRows(),mat.getNumCols());

        if( bits() == 64 ) {
            CommonOps_R64.elementDiv((DMatrixRow_F64)mat, (DMatrixRow_F64)b.getMatrix(), (DMatrixRow_F64)c.getMatrix());
        } else {
            CommonOps_R32.elementDiv((DMatrixRow_F32)mat, (DMatrixRow_F32)b.getMatrix(), (DMatrixRow_F32)c.getMatrix());
        }

        return c;
    }

    /**
     * <p>
     * Returns a matrix which is the result of an element by element power of 'this' and 'b':
     * c<sub>i,j</sub> = a<sub>i,j</sub> ^ b<sub>i,j</sub>
     * </p>
     *
     * @param b A simple matrix.
     * @return The element by element power of 'this' and 'b'.
     */
    public T elementPower( T b )
    {
        T c = createMatrix(mat.getNumRows(),mat.getNumCols());

        if( bits() == 64 ) {
            CommonOps_R64.elementPower((DMatrixRow_F64)mat, (DMatrixRow_F64)b.getMatrix(), (DMatrixRow_F64)c.getMatrix());
        } else {
            CommonOps_R32.elementPower((DMatrixRow_F32)mat, (DMatrixRow_F32)b.getMatrix(), (DMatrixRow_F32)c.getMatrix());
        }

        return c;
    }

    /**
     * <p>
     * Returns a matrix which is the result of an element by element power of 'this' and 'b':
     * c<sub>i,j</sub> = a<sub>i,j</sub> ^ b
     * </p>
     *
     * @param b Scalar
     * @return The element by element power of 'this' and 'b'.
     */
    public T elementPower( double b )
    {
        T c = createMatrix(mat.getNumRows(),mat.getNumCols());

        if( bits() == 64 ) {
            CommonOps_R64.elementPower((DMatrixRow_F64)mat, b, (DMatrixRow_F64)c.getMatrix());
        } else {
            CommonOps_R32.elementPower((DMatrixRow_F32)mat, (float)b, (DMatrixRow_F32)c.getMatrix());
        }

        return c;
    }

    /**
     * <p>
     * Returns a matrix which is the result of an element by element exp of 'this'
     * c<sub>i,j</sub> = Math.exp(a<sub>i,j</sub>)
     * </p>
     *
     * @return The element by element power of 'this' and 'b'.
     */
    public T elementExp()
    {
        T c = createMatrix(mat.getNumRows(),mat.getNumCols());

        if( bits() == 64 ) {
            CommonOps_R64.elementExp((DMatrixRow_F64)mat, (DMatrixRow_F64)c.getMatrix());
        } else {
            CommonOps_R32.elementExp((DMatrixRow_F32)mat, (DMatrixRow_F32)c.getMatrix());
        }

        return c;
    }

    /**
     * <p>
     * Returns a matrix which is the result of an element by element exp of 'this'
     * c<sub>i,j</sub> = Math.log(a<sub>i,j</sub>)
     * </p>
     *
     * @return The element by element power of 'this' and 'b'.
     */
    public T elementLog()
    {
        T c = createMatrix(mat.getNumRows(),mat.getNumCols());

        if( bits() == 64 ) {
            CommonOps_R64.elementLog((DMatrixRow_F64)mat, (DMatrixRow_F64)c.getMatrix());
        } else {
            CommonOps_R32.elementLog((DMatrixRow_F32)mat, (DMatrixRow_F32)c.getMatrix());
        }

        return c;
    }

    /**
     * <p>
     * Returns a new matrix whose elements are the negative of 'this' matrix's elements.<br>
     * <br>
     * b<sub>ij</sub> = -a<sub>ij</sub>
     * </p>
     *
     * @return A matrix that is the negative of the original.
     */
    public T negative() {
        T A = copy();
        if( bits() == 64 ) {
            CommonOps_R64.changeSign((DMatrixRow_F64)A.getMatrix());
        } else {
            CommonOps_R32.changeSign((DMatrixRow_F32)A.getMatrix());
        }
        return A;
    }

    /**
     * <p>
     * Saves this matrix to a file as a serialized binary object.
     * </p>
     *
     * @see MatrixIO#saveBin( Matrix_F64, String)
     *
     * @param fileName
     * @throws java.io.IOException
     */
    public void saveToFileBinary( String fileName )
        throws IOException
    {
        MatrixIO.saveBin((DMatrixRow_F64)mat, fileName);
    }

    /**
     * <p>
     * Loads a new matrix from a serialized binary file.
     * </p>
     *
     * @see MatrixIO#loadBin(String)
     *
     * @param fileName File which is to be loaded.
     * @return The matrix.
     * @throws IOException
     */
    public static SimpleMatrix loadBinary( String fileName )
            throws IOException {
        Matrix_F64 mat = MatrixIO.loadBin(fileName);

        // see if its a DMatrixRow_F64
        if( mat instanceof DMatrixRow_F64) {
            return SimpleMatrix.wrap((DMatrixRow_F64)mat);
        } else {
            // if not convert it into one and wrap it
            return SimpleMatrix.wrap( new DMatrixRow_F64(mat));
        }
    }

    /**
     * <p>
     * Saves this matrix to a file in a CSV format.  For the file format see {@link MatrixIO}.
     * </p>
     *
     * @see MatrixIO#saveBin( Matrix_F64, String)
     *
     * @param fileName
     * @throws java.io.IOException
     */
    public void saveToFileCSV( String fileName )
            throws IOException
    {
        MatrixIO.saveCSV((DMatrixRow_F64)mat, fileName);
    }

    /**
     * <p>
     * Loads a new matrix from a CSV file.  For the file format see {@link MatrixIO}.
     * </p>
     *
     * @see MatrixIO#loadCSV(String)
     *
     * @param fileName File which is to be loaded.
     * @return The matrix.
     * @throws IOException
     */
    public T loadCSV( String fileName )
            throws IOException {
        Matrix_F64 mat = MatrixIO.loadCSV(fileName);

        T ret = createMatrix(1,1);

        // see if its a DMatrixRow_F64
        if( mat instanceof DMatrixRow_F64) {
            ret.mat = (DMatrixRow_F64)mat;
        } else {
            // if not convert it into one and wrap it
            ret.mat = new DMatrixRow_F64(mat);
        }
        return ret;
    }

    /**
     * Returns true of the specified matrix element is valid element inside this matrix.
     * 
     * @param row Row index.
     * @param col Column index.
     * @return true if it is a valid element in the matrix.
     */
    public boolean isInBounds(int row, int col) {
        return row >= 0 && col >= 0 && row < mat.getNumRows() && col < mat.getNumCols();
    }

    /**
     * Prints the number of rows and column in this matrix.
     */
    public void printDimensions() {
        System.out.println("[rows = "+numRows()+" , cols = "+numCols()+" ]");
    }

    /**
     * Size of internal array elements.  32 or 64 bits
     */
    public int bits() {
        if( mat.getClass() == DMatrixRow_F64.class ) {
            return 64;
        } if( mat.getClass() == DMatrixRow_F32.class ) {
            return 32;
        } else {
            throw new RuntimeException("Unknown matrix type");
        }
    }
}
