package com.beyond23.orderSystem.product.service;

import com.beyond23.orderSystem.member.domain.Member;
import com.beyond23.orderSystem.member.repository.MemberRepository;
import com.beyond23.orderSystem.product.domains.Product;
import com.beyond23.orderSystem.product.dtos.ProductCreateDto;
import com.beyond23.orderSystem.product.dtos.ProductResDto;
import com.beyond23.orderSystem.product.dtos.ProductSearchDto;
import com.beyond23.orderSystem.product.dtos.ProductUpdateDto;
import com.beyond23.orderSystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional
public class ProductService {
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final S3Client s3Client;
    private final RedisTemplate<String,String> redisTemplate;
//    yml파일에서 bucket1 정보 연결
    @Value("${aws.s3.bucket1}")
    private String bucket;

    @Autowired
    public ProductService(ProductRepository productRepository, MemberRepository memberRepository, S3Client s3Client,@Qualifier("stockInventory") RedisTemplate<String, String> redisTemplate) {
        this.productRepository = productRepository;
        this.memberRepository = memberRepository;
        this.s3Client = s3Client;   //이미지등록시 필요
        this.redisTemplate = redisTemplate;
    }


    public Long save(ProductCreateDto dto){
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("member is not found"));
        Product product = productRepository.save(dto.toEntity(member));
        if(dto.getProductImage() != null){
            String fileName = "product-" + product.getId() + "-" + dto.getProductImage().getOriginalFilename(); //이미지 여러개일시 for문으로 List에 담아야함
            //      aws s3버킷에 파일 업로드를 할 요청정보를 담는 객체
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)  //이름이 중복되면 엎어씌어짐
                    .contentType(dto.getProductImage().getContentType())
                    .build();
            try{
                s3Client.putObject(request, RequestBody.fromBytes(dto.getProductImage().getBytes()));
            }catch (IOException e){
                throw new RuntimeException(e);
            }
            String imgUrl = s3Client.utilities().getUrl(a->a.bucket(bucket).key(fileName)).toExternalForm();
            product.updateProfileImageUrl(imgUrl);
            System.out.println(imgUrl);
        }

//        동시성문제 해결을 위해 상품등록시 redis에 재고세팅
        redisTemplate.opsForValue().set(String.valueOf(product.getId()), String.valueOf(product.getStockQuantity()));
        return product.getId();
    }


    @Transactional(readOnly = true)
    public Page<ProductResDto> findAll(Pageable pageable, ProductSearchDto searchDto){
        System.out.println("category : " + searchDto.getCategory());
//        동적인 검색 조건을 코드로 조립해서 조회하는 방식 => where조건을 코드로 만드는 인터페이스(JPA조회기법)
        Specification<Product> specification = new Specification<Product>() {
            @Override
            public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicateList = new ArrayList<>();
                if (searchDto.getProductName() != null) {
                    predicateList.add(criteriaBuilder.like(root.get("name"), "%"+searchDto.getProductName()+"%"));
                }
                if (searchDto.getCategory() != null) {
                    predicateList.add(criteriaBuilder.equal(root.get("category"), searchDto.getCategory()));
                }
                Predicate[] predicateArr = new Predicate[predicateList.size()];
                for (int i=0; i<predicateArr.length; i++){
                    predicateArr[i] = predicateList.get(i);
                }
//                Predicate에는 검색조건들이 담길것이고, 이 Predicate list를 한줄의 predicate로 조립
                Predicate predicate = criteriaBuilder.and(predicateArr);
                return predicate;
            }
        };
        Page<Product> productList = productRepository.findAll(specification, pageable);
        return productList.map(p->ProductResDto.fromEntity(p));

        }


    @Transactional(readOnly = true)
    public ProductResDto findById(Long id){
        Product product = productRepository.findById(id).orElseThrow(()->new EntityNotFoundException("상품정보 없음"));
        return ProductResDto.fromEntity(product);
    }

    public void update (Long id, ProductUpdateDto dto){
        Product product = productRepository.findById(id).orElseThrow(()->new EntityNotFoundException("상품정보 없음"));
        product.updateProduct(dto);

        if(dto.getProductImage()!=null){
//            이미지를 수정하는 경우 : 삭제 후 추가
//            기존이미지를 파일명으로 삭제
            if(product.getImagePath() !=null){      //기존 이미지가 null이 아니면 삭제
                String imgUrl = product.getImagePath();
                String fileName = imgUrl.substring(imgUrl.lastIndexOf("/")+1);
                s3Client.deleteObject(a->a.bucket(bucket).key(fileName));
            }
//            신규이미지를 등록
            String newFileName = "product-" + product.getId() + "-" + dto.getProductImage().getOriginalFilename();
//      aws s3버킷에 파일 업로드를 할 요청정보를 담는 객체
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(newFileName)  //이름이 중복되면 엎어씌어짐
                    .contentType(dto.getProductImage().getContentType())
                    .build();
            try{
                s3Client.putObject(request, RequestBody.fromBytes(dto.getProductImage().getBytes()));
            }catch (IOException e){
                throw new RuntimeException(e);
            }
            String newImgUrl = s3Client.utilities().getUrl(a->a.bucket(bucket).key(newFileName)).toExternalForm();
            product.updateProfileImageUrl(newImgUrl);

        } else {
        //            이미지를 삭제하고자 하는 경우
        if (product.getImagePath() != null) {      //기존 이미지가 null이 아니면 삭제
            String imgUrl = product.getImagePath();
            String fileName = imgUrl.substring(imgUrl.lastIndexOf("/") + 1);
            s3Client.deleteObject(a -> a.bucket(bucket).key(fileName));
            }
        }
    }
}
