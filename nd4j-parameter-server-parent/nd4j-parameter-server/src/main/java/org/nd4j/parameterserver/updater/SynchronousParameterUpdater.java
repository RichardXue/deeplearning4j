package org.nd4j.parameterserver.updater;

import org.nd4j.aeron.ipc.NDArrayHolder;
import org.nd4j.aeron.ipc.NDArrayMessage;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.parameterserver.updater.storage.UpdateStorage;
import org.nd4j.shade.jackson.core.JsonProcessingException;
import org.nd4j.shade.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Adds the 2 arrays together,
 * synchronizing when
 * all updates have been collected.
 *
 * @author Adam Gibson
 */
public class SynchronousParameterUpdater extends BaseParameterUpdater {

    private int workers = Runtime.getRuntime().availableProcessors();
    private static ObjectMapper objectMapper = new ObjectMapper();

    /**
     *
     * @param updateStorage
     * @param ndArrayHolder
     * @param workers
     */
    public SynchronousParameterUpdater(UpdateStorage updateStorage, NDArrayHolder ndArrayHolder, int workers) {
        super(updateStorage, ndArrayHolder);
        this.workers = workers;
    }



    /**
     * Returns the current status of this parameter server
     * updater
     *
     * @return
     */
    @Override
    public Map<String, Number> status() {
        Map<String,Number> ret = new HashMap<>();
        ret.put("workers",workers);
        ret.put("accumulatedUpdates",numUpdates());
        return ret;
    }

    /**
     * Serialize this updater as json
     *
     * @return
     */
    @Override
    public String toJson() {
        try {
            return objectMapper.writeValueAsString(status());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Returns true if
     * the updater has accumulated enough ndarrays to
     * replicate to the workers
     *
     * @return true if replication should happen,false otherwise
     */
    @Override
    public boolean shouldReplicate() {
        return numUpdates() == workers;
    }

    /**
     * Do an update based on the ndarray message.
     *
     * @param message
     */
    @Override
    public void update(NDArrayMessage message) {
        updateStorage.addUpdate(message);
        INDArray arr = message.getArr();
        //of note for ndarrays
        int[] dimensions = message.getDimensions();
        boolean whole = dimensions.length == 1 && dimensions[0] == -1;

        if(!whole)
            partialUpdate(arr,ndArrayHolder.get(),message.getIndex(),dimensions);
        else
            update(arr,ndArrayHolder.get());
    }

    /**
     * Updates result
     * based on arr along a particular
     * {@link INDArray#tensorAlongDimension(int, int...)}
     *
     * @param arr        the array to update
     * @param result     the result ndarray to update
     * @param idx        the index to update
     * @param dimensions the dimensions to update
     */
    @Override
    public void partialUpdate(INDArray arr, INDArray result, long idx, int... dimensions) {
        result.tensorAlongDimension((int) idx,dimensions).addi(arr);
    }

    /**
     * Updates result
     * based on arr
     *
     * @param arr    the array to update
     * @param result the result ndarray to update
     */
    @Override
    public void update(INDArray arr, INDArray result) {
        result.addi(arr);
    }
}
