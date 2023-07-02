package edu.npu.service;

import edu.npu.dto.BasicQueryDto;
import edu.npu.entity.AccountUserDetails;
import edu.npu.vo.R;

public interface QueryService {
    R querySenderOutbox(AccountUserDetails accountUserDetails,
                        BasicQueryDto queryDto);

    R queryReceiverInbox(AccountUserDetails accountUserDetails, BasicQueryDto queryDto);
}
