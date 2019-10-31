package co.smartobjects.visitscreator.utils.services;

import android.util.Pair;

/**
 *
 * Created by Jorge on 24/08/2016.
 */
public interface POSTResponseProcessor<T> {
    void processPOSTResult(T postedObject, Pair<Integer, String> result);
}
