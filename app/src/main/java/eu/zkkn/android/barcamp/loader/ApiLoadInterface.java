package eu.zkkn.android.barcamp.loader;

/**
 * Interface for Loaders that can obtain its data from REST API
 */
public interface ApiLoadInterface {

    /**
     * Get data from API. It might be ignored, if forceReload is false.
     * For example, to save a network request when the data are fresh enough.
     * @param forceReload If forceReload is true, a fresh data must be loaded from API
     */
    void loadFromApi(boolean forceReload);

}
