package edu.npu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 住宿职工表
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    /**
     * 住宿职工唯一ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 逻辑外键，与login_account表中id字段构成一一对应关系。也是用户的手机号
     */
    private Long loginAccountId;

    /**
     * 逻辑外键。用户所在外部单位ID。
     */
    private Long departmentId;

    /**
     * 逻辑外键。与bed表对应的床ID。
     */
    private Long bedId;

    /**
     * 用户名称，应当与身份证上的人名一致
     */
    private String name;

    /**
     * 身份证号
     */
    private String personalId;

    /**
     * 身份证正面照片存储URL
     */
    private String personalCardUrl;

    /**
     * 职工人脸照片URL
     */
    private String faceUrl;

    /**
     * 职工支付宝uuid
     */
    private String alipayId;

    /**
     * 职工邮箱
     */
    private String email;

    /**
     * 性别 0男 1女
     */
    private Integer sex;

    /**
     * 是否处级干部 0非 1是
     */
    private Integer isCadre;

    /**
     * 用户是否入住。 0未入住 1申请中 2已入住
     */
    private Integer status;

    /**
     * 缴费类型 0代扣 1自收
     */
    private Integer payType;

    /**
     * 是否需要缴纳网费 0非 1是
     */
    private Integer networkEnabled;

    /**
     * 账号是否已删除 0未删除 1已删除
     */
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        User other = (User) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getLoginAccountId() == null ? other.getLoginAccountId() == null : this.getLoginAccountId().equals(other.getLoginAccountId()))
            && (this.getDepartmentId() == null ? other.getDepartmentId() == null : this.getDepartmentId().equals(other.getDepartmentId()))
            && (this.getBedId() == null ? other.getBedId() == null : this.getBedId().equals(other.getBedId()))
            && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
            && (this.getPersonalId() == null ? other.getPersonalId() == null : this.getPersonalId().equals(other.getPersonalId()))
            && (this.getPersonalCardUrl() == null ? other.getPersonalCardUrl() == null : this.getPersonalCardUrl().equals(other.getPersonalCardUrl()))
            && (this.getFaceUrl() == null ? other.getFaceUrl() == null : this.getFaceUrl().equals(other.getFaceUrl()))
            && (this.getAlipayId() == null ? other.getAlipayId() == null : this.getAlipayId().equals(other.getAlipayId()))
            && (this.getEmail() == null ? other.getEmail() == null : this.getEmail().equals(other.getEmail()))
            && (this.getSex() == null ? other.getSex() == null : this.getSex().equals(other.getSex()))
            && (this.getIsCadre() == null ? other.getIsCadre() == null : this.getIsCadre().equals(other.getIsCadre()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getPayType() == null ? other.getPayType() == null : this.getPayType().equals(other.getPayType()))
            && (this.getNetworkEnabled() == null ? other.getNetworkEnabled() == null : this.getNetworkEnabled().equals(other.getNetworkEnabled()))
            && (this.getIsDeleted() == null ? other.getIsDeleted() == null : this.getIsDeleted().equals(other.getIsDeleted()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getLoginAccountId() == null) ? 0 : getLoginAccountId().hashCode());
        result = prime * result + ((getDepartmentId() == null) ? 0 : getDepartmentId().hashCode());
        result = prime * result + ((getBedId() == null) ? 0 : getBedId().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getPersonalId() == null) ? 0 : getPersonalId().hashCode());
        result = prime * result + ((getPersonalCardUrl() == null) ? 0 : getPersonalCardUrl().hashCode());
        result = prime * result + ((getFaceUrl() == null) ? 0 : getFaceUrl().hashCode());
        result = prime * result + ((getAlipayId() == null) ? 0 : getAlipayId().hashCode());
        result = prime * result + ((getEmail() == null) ? 0 : getEmail().hashCode());
        result = prime * result + ((getSex() == null) ? 0 : getSex().hashCode());
        result = prime * result + ((getIsCadre() == null) ? 0 : getIsCadre().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getPayType() == null) ? 0 : getPayType().hashCode());
        result = prime * result + ((getNetworkEnabled() == null) ? 0 : getNetworkEnabled().hashCode());
        result = prime * result + ((getIsDeleted() == null) ? 0 : getIsDeleted().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", loginAccountId=").append(loginAccountId);
        sb.append(", departmentId=").append(departmentId);
        sb.append(", bedId=").append(bedId);
        sb.append(", name=").append(name);
        sb.append(", personalId=").append(personalId);
        sb.append(", personalCardUrl=").append(personalCardUrl);
        sb.append(", faceUrl=").append(faceUrl);
        sb.append(", alipayId=").append(alipayId);
        sb.append(", email=").append(email);
        sb.append(", sex=").append(sex);
        sb.append(", isCadre=").append(isCadre);
        sb.append(", status=").append(status);
        sb.append(", payType=").append(payType);
        sb.append(", networkEnabled=").append(networkEnabled);
        sb.append(", isDeleted=").append(isDeleted);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}