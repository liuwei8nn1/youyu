package com.youyu.user.impl.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youyu.framework.datasource.mybatis.BaseRepositoryImpl;
import com.youyu.framework.datasource.mybatis.SmartQueryWrapper;
import com.youyu.framework.web.util.PageUtil;
import com.youyu.user.impl.domain.model.Employee;
import com.youyu.user.impl.domain.repository.EmployeeRepository;
import com.youyu.user.impl.infrastructure.persistence.converter.EmployeeConverter;
import com.youyu.user.impl.infrastructure.persistence.entity.EmployeeDO;
import com.youyu.user.impl.infrastructure.persistence.mapper.EmployeeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

/**
 * 企业员工资料仓储实现
 */
@Slf4j
@Repository
public class EmployeeRepositoryImpl extends BaseRepositoryImpl<EmployeeDO, EmployeeMapper, Long> implements EmployeeRepository {

    @Override
    public Long save(Employee employee) {
        EmployeeDO employeeDO = EmployeeConverter.INSTANCE.toDO(employee);
        baseDao.insert(employeeDO);
        log.info("企业员工资料保存成功，identityId: {}", employeeDO.getIdentityId());
        return employeeDO.getId();
    }

    @Override
    public void update(Employee employee) {
        EmployeeDO employeeDO = EmployeeConverter.INSTANCE.toDO(employee);
        baseDao.updateById(employeeDO);
        log.info("企业员工资料更新成功，id: {}", employee.getId());
    }

    @Override
    public Optional<Employee> findById(Long id) {
        EmployeeDO employeeDO = baseDao.selectById(id);
        return Optional.ofNullable(EmployeeConverter.INSTANCE.toDomain(employeeDO));
    }

    @Override
    public Optional<Employee> findByUserId(Long identityId) {
        SmartQueryWrapper<EmployeeDO> wrapper = new SmartQueryWrapper<EmployeeDO>()
                .eq(EmployeeDO.IDENTITY_ID, identityId);
        EmployeeDO employeeDO = baseDao.selectOne(wrapper);
        return Optional.ofNullable(EmployeeConverter.INSTANCE.toDomain(employeeDO));
    }

    @Override
    public Optional<Employee> findByUsername(String username) {
        SmartQueryWrapper<EmployeeDO> wrapper = new SmartQueryWrapper<EmployeeDO>()
                .eq(EmployeeDO.USERNAME, username);
        EmployeeDO employeeDO = baseDao.selectOne(wrapper);
        return Optional.ofNullable(EmployeeConverter.INSTANCE.toDomain(employeeDO));
    }

    @Override
    public Optional<Employee> findByPhone(String phone) {
        SmartQueryWrapper<EmployeeDO> wrapper = new SmartQueryWrapper<EmployeeDO>()
                .eq(EmployeeDO.PHONE, phone);
        EmployeeDO employeeDO = baseDao.selectOne(wrapper);
        return Optional.ofNullable(EmployeeConverter.INSTANCE.toDomain(employeeDO));
    }

    @Override
    public Optional<Employee> findByEmail(String email) {
        SmartQueryWrapper<EmployeeDO> wrapper = new SmartQueryWrapper<EmployeeDO>()
                .eq(EmployeeDO.EMAIL, email);
        EmployeeDO employeeDO = baseDao.selectOne(wrapper);
        return Optional.ofNullable(EmployeeConverter.INSTANCE.toDomain(employeeDO));
    }

    @Override
    public boolean existsByUsername(String username) {
        SmartQueryWrapper<EmployeeDO> wrapper = new SmartQueryWrapper<EmployeeDO>()
                .eq(EmployeeDO.USERNAME, username);
        return baseDao.selectCount(wrapper) > 0;
    }

    @Override
    public boolean existsByPhone(String phone) {
        SmartQueryWrapper<EmployeeDO> wrapper = new SmartQueryWrapper<EmployeeDO>()
                .eq(EmployeeDO.PHONE, phone);
        return baseDao.selectCount(wrapper) > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        SmartQueryWrapper<EmployeeDO> wrapper = new SmartQueryWrapper<EmployeeDO>()
                .eq(EmployeeDO.EMAIL, email);
        return baseDao.selectCount(wrapper) > 0;
    }

    @Override
    public Page<Employee> listPage(Page<EmployeeDO> page, String keyword, Long deptId, Integer status) {
        SmartQueryWrapper<EmployeeDO> wrapper = new SmartQueryWrapper<EmployeeDO>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(EmployeeDO.USERNAME, keyword)
                    .or().like(EmployeeDO.PHONE, keyword)
                    .or().like(EmployeeDO.EMAIL, keyword)
            );
        }
        if (deptId != null) {
            wrapper.eq(EmployeeDO.DEPT_ID, deptId);
        }
        if (status != null) {
            wrapper.eq(EmployeeDO.STATUS, status);
        }

        log.debug("查询员工列表 - page: {}, size: {}, keyword: {}, deptId: {}, status: {}", 
                page.getCurrent(), page.getSize(), keyword, deptId, status);
        
        Page<EmployeeDO> doPage = baseDao.selectPage(page, wrapper);
        
        log.debug("查询结果 - total: {}, records count: {}", doPage.getTotal(), 
                doPage.getRecords() != null ? doPage.getRecords().size() : 0);
        
        // 使用 PageUtil 转换，不创建新对象
        return PageUtil.convert(doPage, EmployeeConverter.INSTANCE::toDomain);
    }

    @Override
    public void removeById(Long id) {
        baseDao.deleteById(id);
        log.info("企业员工资料删除成功，id: {}", id);
    }

    @Override
    public List<Employee> listAll() {
        List<EmployeeDO> doList = baseDao.selectList(null);
        return doList.stream()
                .map(EmployeeConverter.INSTANCE::toDomain)
                .collect(Collectors.toList());
    }
}
