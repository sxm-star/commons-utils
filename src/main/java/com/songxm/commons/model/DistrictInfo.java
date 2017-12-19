package com.songxm.commons.model;

public class DistrictInfo {
    private String province;
    private String city;
    private String county;

    public DistrictInfo() {
    }

    public DistrictInfo(String province, String city, String county) {
        this.province = province;
        this.city = city;
        this.county = county;
    }

    public String getProvince() {
        return this.province;
    }

    public String getCity() {
        return this.city;
    }

    public String getCounty() {
        return this.county;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    @Override
    public boolean equals(Object o) {
        if(o == this) {
            return true;
        } else if(!(o instanceof DistrictInfo)) {
            return false;
        } else {
            DistrictInfo other = (DistrictInfo)o;
            if(!other.canEqual(this)) {
                return false;
            } else {
                label47: {
                    String this$province = this.getProvince();
                    String other$province = other.getProvince();
                    if(this$province == null) {
                        if(other$province == null) {
                            break label47;
                        }
                    } else if(this$province.equals(other$province)) {
                        break label47;
                    }

                    return false;
                }

                String this$city = this.getCity();
                String other$city = other.getCity();
                if(this$city == null) {
                    if(other$city != null) {
                        return false;
                    }
                } else if(!this$city.equals(other$city)) {
                    return false;
                }

                String this$county = this.getCounty();
                String other$county = other.getCounty();
                if(this$county == null) {
                    if(other$county != null) {
                        return false;
                    }
                } else if(!this$county.equals(other$county)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof DistrictInfo;
    }

    @Override
    public int hashCode() {
        boolean PRIME = true;
        byte result = 1;
        String $province = this.getProvince();
        int result1 = result * 59 + ($province == null?0:$province.hashCode());
        String $city = this.getCity();
        result1 = result1 * 59 + ($city == null?0:$city.hashCode());
        String $county = this.getCounty();
        result1 = result1 * 59 + ($county == null?0:$county.hashCode());
        return result1;
    }

    @Override
    public String toString() {
        return "DistrictInfo(province=" + this.getProvince() + ", city=" + this.getCity() + ", county=" + this.getCounty() + ")";
    }
}
