package com.songxm.commons.model;

public class IdCardInfo {
    private String province;
    private String city;
    private String county;
    private String birthDate;
    private Integer age;
    private boolean isMale;

    public IdCardInfo() {
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

    public String getBirthDate() {
        return this.birthDate;
    }

    public Integer getAge() {
        return this.age;
    }

    public boolean isMale() {
        return this.isMale;
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

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setMale(boolean isMale) {
        this.isMale = isMale;
    }

    @Override
    public boolean equals(Object o) {
        if(o == this) {
            return true;
        } else if(!(o instanceof IdCardInfo)) {
            return false;
        } else {
            IdCardInfo other = (IdCardInfo)o;
            if(!other.canEqual(this)) {
                return false;
            } else {
                label75: {
                    String this$province = this.getProvince();
                    String other$province = other.getProvince();
                    if(this$province == null) {
                        if(other$province == null) {
                            break label75;
                        }
                    } else if(this$province.equals(other$province)) {
                        break label75;
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

                label54: {
                    String this$birthDate = this.getBirthDate();
                    String other$birthDate = other.getBirthDate();
                    if(this$birthDate == null) {
                        if(other$birthDate == null) {
                            break label54;
                        }
                    } else if(this$birthDate.equals(other$birthDate)) {
                        break label54;
                    }

                    return false;
                }

                label47: {
                    Integer this$age = this.getAge();
                    Integer other$age = other.getAge();
                    if(this$age == null) {
                        if(other$age == null) {
                            break label47;
                        }
                    } else if(this$age.equals(other$age)) {
                        break label47;
                    }

                    return false;
                }

                if(this.isMale() != other.isMale()) {
                    return false;
                } else {
                    return true;
                }
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof IdCardInfo;
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
        String $birthDate = this.getBirthDate();
        result1 = result1 * 59 + ($birthDate == null?0:$birthDate.hashCode());
        Integer $age = this.getAge();
        result1 = result1 * 59 + ($age == null?0:$age.hashCode());
        result1 = result1 * 59 + (this.isMale()?79:97);
        return result1;
    }

    @Override
    public String toString() {
        return "IdCardInfo(province=" + this.getProvince() + ", city=" + this.getCity() + ", county=" + this.getCounty() + ", birthDate=" + this.getBirthDate() + ", age=" + this.getAge() + ", isMale=" + this.isMale() + ")";
    }
}
