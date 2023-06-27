package edu.npu.doc;

import com.fasterxml.jackson.annotation.JsonFormat;
import edu.npu.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : [wangminan]
 * @description : [{@link edu.npu.entity.User}在ElasticSearch中的Mapping映射]
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDoc {

    /**
     * 住宿职工唯一ID
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /**
     * 逻辑外键，与login_account表中id字段构成一一对应关系。也是用户的手机号
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long loginAccountId;

    /**
     * 逻辑外键。用户所在外部单位ID。
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long departmentId;

    /**
     * 逻辑外键。与bed表对应的床ID。
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long bedId;

    /**
     * 用户名称，应当与身份证上的人名一致
     */
    private String name;

    /**
     * 身份证号
     */
    private String personalId;

    public UserDoc(User user){
        this.id = user.getId();
        this.loginAccountId = user.getLoginAccountId();
        this.departmentId = user.getDepartmentId();
        this.bedId = user.getBedId();
        this.name = user.getName();
        this.personalId = user.getPersonalId();
    }
}
