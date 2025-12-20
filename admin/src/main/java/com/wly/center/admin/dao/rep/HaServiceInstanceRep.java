package com.wly.center.admin.dao.rep;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wly.center.admin.dao.entity.HaServiceInstance;
import com.wly.center.admin.dao.mapper.HaServiceInstanceMapper;
import org.springframework.stereotype.Repository;

@Repository
public class HaServiceInstanceRep extends ServiceImpl<HaServiceInstanceMapper, HaServiceInstance> {
}
