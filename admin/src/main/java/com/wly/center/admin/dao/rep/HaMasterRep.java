package com.wly.center.admin.dao.rep;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wly.center.admin.dao.entity.HaMasterInstance;
import com.wly.center.admin.dao.mapper.HaMasterInstanceMapper;
import org.springframework.stereotype.Repository;

@Repository
public class HaMasterRep extends ServiceImpl<HaMasterInstanceMapper, HaMasterInstance> {
}
