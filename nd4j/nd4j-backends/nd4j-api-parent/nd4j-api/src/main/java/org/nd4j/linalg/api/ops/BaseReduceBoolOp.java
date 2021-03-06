/*******************************************************************************
 * Copyright (c) 2015-2018 Skymind, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ******************************************************************************/

package org.nd4j.linalg.api.ops;

import lombok.val;
import org.nd4j.autodiff.samediff.SDVariable;
import org.nd4j.autodiff.samediff.SameDiff;
import org.nd4j.base.Preconditions;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.shape.LongShapeDescriptor;
import org.nd4j.linalg.api.shape.Shape;
import org.nd4j.linalg.exception.ND4JIllegalArgumentException;
import org.nd4j.linalg.exception.ND4JIllegalStateException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BaseReduceBoolOp extends BaseReduceOp implements ReduceBoolOp {
    public BaseReduceBoolOp(SameDiff sameDiff, SDVariable i_v, SDVariable i_v2, int[] dimensions) {
        super(sameDiff, i_v, i_v2, dimensions);
    }

    protected BaseReduceBoolOp(SameDiff sameDiff, SDVariable input, int[] dimensions, boolean keepDims) {
        super(sameDiff, input, dimensions, keepDims);
    }

    protected BaseReduceBoolOp(SameDiff sameDiff, SDVariable input, int... dimensions) {
        super(sameDiff, input, dimensions);
    }

    public BaseReduceBoolOp(INDArray x, INDArray z, boolean newFormat, boolean keepDims, int[] dimensions) {
        super(x, null, z, newFormat, keepDims, dimensions);
    }

    public BaseReduceBoolOp(INDArray x, int... dimensions) {
        this(x, null, true, false, dimensions);
    }

    public BaseReduceBoolOp(INDArray x, INDArray z, int... dimensions) {
        this(x, z, true, false, dimensions);
    }

    protected BaseReduceBoolOp() {
        super();
    }

    @Override
    public Type opType() {
        return Type.REDUCE_BOOL;
    }

    @Override
    public Type getOpType() {
        return opType();
    }

    @Override
    public DataType resultType() {
        return DataType.BOOL;
    }

    @Override
    public boolean validateDataTypes() {
        if (y() != null)
            Preconditions.checkArgument(x().dataType()  == y().dataType(),"Op.X type must be the same as Op.Y:" +
                            " x.dataType=%s, y.dataType=%s, op=%s", x.dataType(), y.dataType(), getClass().getName());

        if (z() != null)
            Preconditions.checkArgument(z().isB(), "Op.X type must be bool: got type %s for op %s", x.dataType(), getClass());

        return true;
    }

    @Override
    public List<LongShapeDescriptor> calculateOutputShape() {
        if(args().length < 1) {
            throw new ND4JIllegalStateException("Unable to compute input shape. No arguments found.");
        }

        long[] argShape = arg().getShape();
        if (argShape == null && x() == null) {
            return Collections.emptyList();
        }
        long[] inputShape = (argShape == null ? x().shape() : argShape);

        val ret = new ArrayList<LongShapeDescriptor>(1);
        val reducedShape = Shape.getReducedShape(inputShape,dimensions, isKeepDims(), newFormat);
        ret.add(LongShapeDescriptor.fromShape(reducedShape, DataType.BOOL));
        return ret;
    }
}
