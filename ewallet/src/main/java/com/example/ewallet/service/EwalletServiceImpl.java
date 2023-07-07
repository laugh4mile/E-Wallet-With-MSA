package com.example.ewallet.service;

import com.example.ewallet.dto.EwalletDto;
import com.example.ewallet.jpa.EwalletEntity;
import com.example.ewallet.jpa.EwalletRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Slf4j
public class EwalletServiceImpl implements EwalletService {
    @Autowired
    EwalletRepository ewalletRepository;


    @Override
    public EwalletDto createEwallet(EwalletDto ewalletDto) {
        if ("".equals(ewalletDto.getEwalletId()) || ewalletDto.getEwalletId() == null) {
            ewalletDto.setEwalletId(UUID.randomUUID().toString());
        }

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        EwalletEntity ewalletEntity = mapper.map(ewalletDto, EwalletEntity.class);
        ewalletRepository.save(ewalletEntity);
        return null;
    }


    @Override
    public EwalletDto searchEwallet(String userId) {
        EwalletEntity ewalletEntity = ewalletRepository.findByUserId(userId);
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        EwalletDto ewalletDto = mapper.map(ewalletEntity, EwalletDto.class);

        return ewalletDto;
    }

    @Override
    public EwalletDto updateBalance(EwalletDto ewalletDto) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("ewallet");
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        EwalletDto ewalletDto1 = new EwalletDto();
        try {
            ewalletDto1 = em.find(EwalletDto.class, ewalletDto);
            log.info("ewalletDto1 : " + ewalletDto1.toString());

            CriteriaBuilder cb = em.getCriteriaBuilder();

            CriteriaUpdate<EwalletDto> updateCriteria = cb.createCriteriaUpdate(EwalletDto.class);
            Root<EwalletDto> root = updateCriteria.from(EwalletDto.class);

            updateCriteria.set(root.get("amt"), ewalletDto1.getAmt().add(ewalletDto.getAmt()));
            Predicate condition = cb.and(
                    cb.equal(root.get("userId"), ewalletDto.getUserId()),
                    cb.equal(root.get("ewalletId"), ewalletDto.getEwalletId())
            );
            updateCriteria.where(condition);

            // 업데이트 실행
            int updatedCount = em.createQuery(updateCriteria).executeUpdate();

            // 업데이트된 엔티티 수 출력
            log.info("Updated count: " + updatedCount);


            // 엔티티 수정
//            ewalletDto1.setAmt(ewalletDto1.getAmt().add(ewalletDto.getAmt()));

            // merge()를 사용하여 업데이트 수행
//            EwalletDto updateEwallet = em.merge(ewalletDto1);

            // 업데이트된 엔티티 확인

            // 트랜잭션 커밋
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
            em.close();
            emf.close();
        }
//        EwalletEntity ewalletEntity = ewalletRepository.findByUserId(ewalletDto.getUserId());
//
//        ModelMapper mapper = new ModelMapper();
//        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
//
//        EwalletDto ewalletDto1 = mapper.map(ewalletEntity, EwalletDto.class);
//        BigDecimal finalBal = ewalletDto1.getAmt().add(ewalletDto.getAmt());
//        ewalletDto1.setAmt(ewalletDto1.getAmt().add(ewalletDto.getAmt()));
//        EwalletEntity ewalletEntity1 = mapper.map(ewalletDto1, EwalletEntity.class);
//        ewalletRepository.save(ewalletEntity1);

//        ewalletDto = mapper.map(ewalletEntity1, EwalletDto.class);
        return ewalletDto1;
    }
}
