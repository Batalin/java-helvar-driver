/**
 * 
 */
package eu.smartcampus.api.datapointconnectivity;

/**
 * The object representing a datapoint address.
 */
public class DatapointAddress {

    /** The address. */
    private final String address;

    /**
     * The Constructor.
     *
     * @param address the address
     */
    public DatapointAddress(String address) {
        if (address == null)
            throw new IllegalArgumentException("Address cannot be null!");
        this.address = address;
    }

    /**
     * Gets the address.
     *
     * @return the address
     */
    public String getAddress() {
        return this.address;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((address == null) ? 0 : address.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DatapointAddress other = (DatapointAddress) obj;
        return other.address.equals(this.address);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return address;
    }
}
