package datawave.query.iterator.filter;

import datawave.query.config.EdgeQueryConfiguration;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.iterators.Filter;
import org.apache.accumulo.core.iterators.IteratorEnvironment;
import org.apache.accumulo.core.iterators.SortedKeyValueIterator;

import java.io.IOException;
import java.util.Map;
import datawave.edge.util.EdgeKey;

/**
 *
 */
public class DateTypeFilter extends Filter {
    protected EdgeQueryConfiguration.dateType dateType = EdgeQueryConfiguration.dateType.ACQUISITION;
    
    protected boolean[] state;
    
    @Override
    public SortedKeyValueIterator<Key,Value> deepCopy(IteratorEnvironment env) {
        DateTypeFilter result = (DateTypeFilter) super.deepCopy(env);
        result.dateType = dateType;
        result.state = state;
        
        return result;
    }
    
    /**
     * Method to setup expression from the iterator options for evaluation.
     *
     * @param options
     */
    private void initOptions(Map<String,String> options) throws IOException {
        
        String e = options.get(EdgeQueryConfiguration.DATE_RANGE_TYPE);
        if (EdgeQueryConfiguration.dateType.ACTIVITY.name().equals(e) || EdgeQueryConfiguration.dateType.ACTIVITY_LOAD.name().equals(e)) {
            dateType = EdgeQueryConfiguration.dateType.ACTIVITY;
        } else if (EdgeQueryConfiguration.dateType.ANY.name().equals(e) || EdgeQueryConfiguration.dateType.ANY_LOAD.name().equals(e)) {
            dateType = EdgeQueryConfiguration.dateType.ANY;
        } else { // we default to acquisition
            dateType = EdgeQueryConfiguration.dateType.ACQUISITION;
        }
    }
    
    @Override
    public void init(SortedKeyValueIterator<Key,Value> source, Map<String,String> options, IteratorEnvironment env) throws java.io.IOException {
        super.init(source, options, env);
        initOptions(options);
        
        // Pre compute a state transition table so that the accept method can be as fast as possible
        state = new boolean[4];
        
        for (int i = 0; i < state.length; i++) {
            state[i] = true;
        }
        
        if (dateType == EdgeQueryConfiguration.dateType.ACQUISITION) {
            state[EdgeKey.DATE_TYPE.ACTIVITY_ONLY.ordinal()] = false;
        } else if (dateType == EdgeQueryConfiguration.dateType.ACTIVITY) {
            state[EdgeKey.DATE_TYPE.ACQUISITION_ONLY.ordinal()] = false;
            state[EdgeKey.DATE_TYPE.OLD_ACQUISITION.ordinal()] = false;
        }
    }
    
    /**
     * Determines if the edge key matches the desired date type.<br>
     * <strong>note:</strong> Some edges qualify as both an acquisition and an activity edge. Hence, can't simply negate the return value of isAcquisitionEdge
     * or isActivityEdge.
     *
     * @param k
     * @param V
     * @return boolean - true if it is a match.
     */
    @Override
    public boolean accept(Key k, Value V) {
        
        return (state[EdgeKey.getDateType(k).ordinal()]);
    }
    
}
