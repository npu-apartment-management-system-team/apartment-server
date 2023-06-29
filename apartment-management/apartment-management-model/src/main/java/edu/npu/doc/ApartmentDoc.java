package edu.npu.doc;

import edu.npu.entity.Apartment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : [wangminan]
 * @description : [{@link edu.npu.entity.Apartment} 在ES中的mapping]
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApartmentDoc {

    private Long id;

    /**
     * 负责管理该公寓的主要班组长的在admin表中的id
     */
    private Long foremanAdminId;

    /**
     * 公寓名称，eg.望江门公寓
     */
    private String name;

    /**
     * 公寓所在具体地名,从市一级开始精确到门牌号
     */
    private String position;

    /**
     * 经纬度
     */
    private String location;

    public ApartmentDoc(Apartment apartment) {
        this.id = apartment.getId();
        this.foremanAdminId = apartment.getForemanAdminId();
        this.name = apartment.getName();
        this.position = apartment.getPosition();
        this.location = apartment.getPositionLatitude() + "," + apartment.getPositionLongitude();
    }
}
