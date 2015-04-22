/*
 * Copyright 2015 Skymind,Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.nd4j.linalg.fft;

import org.nd4j.linalg.api.complex.IComplexNDArray;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.transforms.VectorFFT;
import org.nd4j.linalg.api.ops.impl.transforms.VectorIFFT;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.util.ArrayUtil;
import org.nd4j.linalg.util.ComplexNDArrayUtil;

/**
 * Default FFT instance that will work that is backend agnostic.
 *
 * @author Adam Gibson
 */
public class DefaultFFTInstance extends BaseFFTInstance {

    /**
     * FFT along a particular dimension
     *
     * @param transform   the ndarray to op
     * @param numElements the desired number of elements in each fft
     * @return the ffted output
     */
    @Override
    public IComplexNDArray fft(INDArray transform, int numElements, int dimension) {
        IComplexNDArray inputC = Nd4j.createComplex(transform);
        if (inputC.isVector())
            return (IComplexNDArray) Nd4j.getExecutioner().execAndReturn(new VectorFFT(inputC,numElements));
        else {
            return rawfft(inputC, numElements, dimension);
        }
    }

    /**
     * 1d discrete fourier op, note that this will
     * throw an exception if the passed in input
     * isn't a vector.
     * See matlab's fft2 for more information
     *
     * @param inputC the input to op
     * @return the the discrete fourier op of the passed in input
     */
    @Override
    public IComplexNDArray fft(IComplexNDArray inputC, int numElements, int dimension) {
        if (inputC.isVector())
            return (IComplexNDArray) Nd4j.getExecutioner().execAndReturn(new VectorFFT(inputC,numElements));
        else {
            return rawfft(inputC, numElements, dimension);
        }
    }


    /**
     * IFFT along a particular dimension
     *
     * @param transform   the ndarray to op
     * @param numElements the desired number of elements in each fft
     * @param dimension   the dimension to do fft along
     * @return the iffted output
     */
    @Override
    public IComplexNDArray ifft(INDArray transform, int numElements, int dimension) {
        IComplexNDArray inputC = Nd4j.createComplex(transform);
        if (inputC.isVector())
            return (IComplexNDArray) Nd4j.getExecutioner().execAndReturn(new VectorIFFT(inputC,numElements));
        else {
            return rawifft(inputC, numElements, dimension);
        }
    }

    /**
     * 1d discrete fourier op, note that this will
     * throw an exception if the passed in input
     * isn't a vector.
     * See matlab's fft2 for more information
     *
     * @param inputC the input to op
     * @return the the discrete fourier op of the passed in input
     */
    @Override
    public IComplexNDArray ifft(IComplexNDArray inputC, int numElements, int dimension) {
        if (inputC.isVector())
            return (IComplexNDArray) Nd4j.getExecutioner().execAndReturn(new VectorIFFT(inputC,numElements));
        else {
            return rawifft(inputC, numElements, dimension);
        }
    }

    /**
     * FFT along a particular dimension
     *
     * @param transform   the ndarray to op
     * @param numElements the desired number of elements in each fft
     * @return the ffted output
     */
    @Override
    public IComplexNDArray ifft(INDArray transform, int numElements) {
        IComplexNDArray inputC = Nd4j.createComplex(transform);
        if (inputC.isVector())
            return (IComplexNDArray) Nd4j.getExecutioner().execAndReturn(new VectorIFFT(inputC,numElements));
        else {
            return rawifft(inputC, numElements, inputC.shape().length - 1);
        }
    }

    /**
     * 1d discrete fourier op, note that this will
     * throw an exception if the passed in input
     * isn't a vector.
     * See matlab's fft2 for more information
     *
     * @param inputC the input to op
     * @return the the discrete fourier op of the passed in input
     */
    @Override
    public IComplexNDArray ifft(IComplexNDArray inputC) {
        if (inputC.isVector())
            return (IComplexNDArray) Nd4j.getExecutioner().execAndReturn(new VectorFFT(inputC,inputC.length()));
        else {
            return rawifft(inputC, inputC.size(inputC.shape().length - 1), inputC.shape().length - 1);
        }
    }


    /**
     * Underlying fft algorithm
     *
     * @param transform the ndarray to op
     * @param n         the desired number of elements
     * @param dimension the dimension to do fft along
     * @return the transformed ndarray
     */
    @Override
    public IComplexNDArray rawfft(IComplexNDArray transform, int n, int dimension) {
        IComplexNDArray result = transform.dup();

        if (transform.size(dimension) != n) {
            int[] shape = ArrayUtil.copy(result.shape());
            shape[dimension] = n;
            if (transform.size(dimension) > n) {
                result = ComplexNDArrayUtil.truncate(result, n, dimension);
            } else
                result = ComplexNDArrayUtil.padWithZeros(result, shape);

        }


        if (dimension != result.shape().length - 1)
            result = result.swapAxes(result.shape().length - 1, dimension);


        Nd4j.getExecutioner().iterateOverAllRows(new VectorFFT(result, result.size(result.shape().length - 1)));

        if (dimension != result.shape().length - 1)
            result = result.swapAxes(result.shape().length - 1, dimension);

        return result;
    }


    //underlying fftn
    @Override
    public IComplexNDArray rawifft(IComplexNDArray transform, int n, int dimension) {
        IComplexNDArray result = transform.dup();

        if (transform.size(dimension) != n) {
            int[] shape = ArrayUtil.copy(result.shape());
            shape[dimension] = n;
            if (transform.size(dimension) > n) {
                result = ComplexNDArrayUtil.truncate(result, n, dimension);
            } else
                result = ComplexNDArrayUtil.padWithZeros(result, shape);

        }


        if (dimension != result.shape().length - 1)
            result = result.swapAxes(result.shape().length - 1, dimension);

        Nd4j.getExecutioner().iterateOverAllRows(new VectorIFFT(result, result.size(result.shape().length - 1)));


        if (dimension != result.shape().length - 1)
            result = result.swapAxes(result.shape().length - 1, dimension);

        return result;
    }

}
