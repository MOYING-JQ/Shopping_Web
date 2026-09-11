package com.moying.project_test.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moying.project_test.dto.ProductDTO;
import com.moying.project_test.dto.ProductPageQueryDTO;
import com.moying.project_test.entity.Product;
import com.moying.project_test.entity.User;
import com.moying.project_test.exception.BusinessException;
import com.moying.project_test.mapper.ProductMapper;
import com.moying.project_test.mapper.UserMapper;
import com.moying.project_test.service.ICategoryService;
import com.moying.project_test.service.IProductService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.moying.project_test.util.FileUploadUtil;
import com.moying.project_test.vo.ProductVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 商品表 服务实现类
 * </p>
 *
 * @author moying
 * @since 2026-08-26
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements IProductService {


    @Autowired
    private ICategoryService categoryService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FileUploadUtil fileUploadUtil;

    @Override
    public IPage<ProductVo> findAll(ProductPageQueryDTO dto) {
        if(dto.getRole() == 1){
            boolean bool = categoryService.findByParentCategory(dto.getCategoryId());
            if(!bool){
                dto.setId(dto.getCategoryId());
                dto.setCategoryId(null);
            }
            long pageNum = dto.getCurrent() == null ? 1L : dto.getCurrent();
            long pageSize = dto.getSize() == null ? 10L : dto.getSize();
            Page<ProductVo> page = new Page<>(pageNum, pageSize);
            page.setOptimizeCountSql(false);

            return baseMapper.selectProductPage(page, dto);
        }
        //排序白名单，后续新增排序字段在这里加
        List<String> allowSort = Arrays.asList("sales", "price", "create_time");
        String sortField = dto.getSortField();
        String sortOrder = dto.getSortOrder();

        //非法排序直接置null，不排序
        if(sortField==null||!allowSort.contains(sortField)){
            dto.setSortField(null);
            dto.setSortOrder(null);
        }else{
            if(!"asc".equalsIgnoreCase(sortOrder)&&!"desc".equalsIgnoreCase(sortOrder)){
                dto.setSortOrder(null);
            }
        }
        long pageNum = dto.getCurrent() == null ? 1L : dto.getCurrent();
        long pageSize = dto.getSize() == null ? 10L : dto.getSize();
        Page<ProductVo> page = new Page<>(pageNum, pageSize);
        page.setOptimizeCountSql(false);
        return baseMapper.selectProductPage(page, dto);
    }


    @Override
    public Boolean addGood(ProductDTO dto, MultipartFile file) {
        //是否是卖家
        // 获取Authentication对象
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 获取用户名
        assert authentication != null;
        String username = authentication.getName();
        Long id = findIdByName(username);

        Byte role = findRoleByName(username);
        if (role == 0) {
            throw new BusinessException("非法访问，你并不是商家");
        }
        //把file转换成url保存在本地和数据库
        try {
            String url = fileUploadUtil.uploadImage(file);
            dto.setImage(url);
        } catch (Exception e) {
            throw new BusinessException("图片上传失败：" + e.getMessage());
        }

        //转product实体对象
        Product product = new Product();
        product.setName(dto.getName());
        product.setSellerId(id);
        product.setCategoryId(dto.getCategoryId());
        product.setPrice(dto.getPrice());
        product.setImage(dto.getImage());
        product.setDescription(dto.getDescription());
        product.setSales(0);
        Byte deleteStatus = 0;
        product.setDeleted(deleteStatus);
        product.setStatus(dto.getStatus());
        product.setStock(dto.getStock());
        product.setOriginalPrice(dto.getOriginalPrice());

        //存数据库
        return baseMapper.insert(product) > 0;
    }

    @Override
    public Boolean updateGoods(ProductDTO dto, MultipartFile file) {

        //是否是卖家
        // 获取Authentication对象
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 获取用户名
        assert authentication != null;
        String username = authentication.getName();
        Long id = findIdByName(username);

        Byte role = findRoleByName(username);
        if (role == 0) {
            throw new BusinessException("非法访问，你并不是商家");
        }
        Product product = new Product();
        //文件是否为空
        if (file != null && !file.isEmpty()) {
            try{
                String url = fileUploadUtil.uploadImage(file);
                product.setImage(url);
            }
            catch (Exception e) {
                throw new BusinessException("图片上传失败：" + e.getMessage());
            }

        }

        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setSellerId(id);
        product.setCategoryId(dto.getCategoryId());
        product.setPrice(dto.getPrice());
        product.setDescription(dto.getDescription());
        Byte deleteStatus = 0;
        product.setDeleted(deleteStatus);
        product.setStatus(dto.getStatus());
        product.setStock(dto.getStock());
        product.setOriginalPrice(dto.getOriginalPrice());

        return baseMapper.updateById(product) > 0;
    }


    @Override
    public boolean updateStatus(Long id, Byte status) {
        return lambdaUpdate()
                .eq(Product::getId,id)
                .setSql("status = #{} " , status )
                .update();
    }

    @Override
    public List<ProductVo> findTop(int limit) {
        //是否是卖家
        // 获取Authentication对象
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 获取用户名
        assert authentication != null;
        String username = authentication.getName();
        Long id = findIdByName(username);

        Byte role = findRoleByName(username);
        if (role == 0) {
            throw new BusinessException("非法访问，你并不是商家");
        }

        List<Product> products =  lambdaQuery()
                .eq(Product::getSellerId,id)
                .orderByDesc(Product::getSales)
                .last("limit " + limit)
                .list();
        List<ProductVo> productVos = new ArrayList<>();

        for (Product product : products) {
            ProductVo productVo = new ProductVo();
            productVo.setId(product.getId());
            productVo.setName(product.getName());
            productVo.setCategoryId(product.getCategoryId());
            String name = categoryService.findNameById(product.getCategoryId());
            productVo.setCategoryName(name);
            productVo.setPrice(product.getPrice());
            productVo.setOriginalPrice(product.getOriginalPrice());
            productVo.setDescription(product.getDescription());
            productVo.setStock(product.getStock());
            productVo.setSales(product.getSales());
            productVo.setImage(product.getImage());
            productVo.setStatus(product.getStatus());

            productVos.add(productVo);

        }

        return productVos;
    }

    @Override
    public List<ProductVo> findHotTop(Integer limit) {
        List<Product> products =  lambdaQuery()
                .orderByDesc(Product::getSales)
                .last("limit " + limit)
                .list();
        List<ProductVo> productVos = new ArrayList<>();

        for (Product product : products) {
            ProductVo productVo = new ProductVo();
            productVo.setId(product.getId());
            productVo.setName(product.getName());
            productVo.setCategoryId(product.getCategoryId());
            String name = categoryService.findNameById(product.getCategoryId());
            productVo.setCategoryName(name);
            productVo.setPrice(product.getPrice());
            productVo.setOriginalPrice(product.getOriginalPrice());
            productVo.setDescription(product.getDescription());
            productVo.setStock(product.getStock());
            productVo.setSales(product.getSales());
            productVo.setImage(product.getImage());
            productVo.setStatus(product.getStatus());

            productVos.add(productVo);

        }

        return productVos;
    }

    @Override
    public boolean decrease(Long productId, Integer quantity) {

        return lambdaUpdate()
                .eq(Product::getId,productId)
                .ge(Product::getStock, quantity)
                .setSql("stock = stock - {0}",quantity)
                .setSql("sales = sales + {0}",quantity)
                .update();
    }

    @Override
    public Long findByProductId(Long productId) {
        return lambdaQuery()
                .eq(Product::getId,productId)
                .one()
                .getSellerId();
    }

    @Override
    public String findImageByProductId(Long productId) {
        return lambdaQuery()
                .eq(Product::getId,productId)
                .one()
                .getImage();
    }

    private Long findIdByName(String username) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        return user != null ? user.getId() : null;
    }

    private Byte findRoleByName(String username) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        return user != null ? user.getRole() : null;
    }
}
