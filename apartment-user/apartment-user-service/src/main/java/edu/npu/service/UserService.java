package edu.npu.service;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.doc.UserDoc;
import edu.npu.dto.BindAlipayCallbackDto;
import edu.npu.dto.UserPageQueryDto;
import edu.npu.dto.UserUpdateDto;
import edu.npu.entity.User;
import edu.npu.vo.R;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @author wangminan
* @description 针对表【user(住宿职工表)】的数据库操作Service
* @createDate 2023-06-27 21:19:32
*/
public interface UserService extends IService<User> {

    R getUsersInfo(UserPageQueryDto userPageQueryDto);

    @Transactional(rollbackFor = Exception.class)
    R updateUserInfo(Long id, UserUpdateDto userUpdateDto);

    @Transactional(rollbackFor = Exception.class)
    R deleteUser(Long id);

    String bindAlipayToUser(BindAlipayCallbackDto bindAlipayCallbackDto);

    SearchRequest buildBasicQuery(UserPageQueryDto pageQueryDto);

    R resolveRestResponse(SearchResponse<UserDoc> response);

    R getUserInfo(Long id);
}
