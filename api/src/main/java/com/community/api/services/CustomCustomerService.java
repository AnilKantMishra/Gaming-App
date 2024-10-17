package com.community.api.services;
import com.community.api.component.Constant;
import com.community.api.dto.PrizeDistributionDTO;
import com.community.api.dto.TournamentDTO;
import com.community.api.dto.UserDetailDTO;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.Participation;
import com.community.api.entity.Prize;
import com.community.api.entity.Tournament;
import org.apache.commons.collections4.CollectionUtils;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CustomCustomerService {
    private EntityManager em;

    private  TournamentService tournamentService;
    public CustomCustomerService(EntityManager em)
    {
        this.em= em;
    }

    public Boolean validateInput(CustomCustomer customer) {
        if (customer.getUsername().isEmpty() || customer.getUsername() == null || customer.getMobileNumber().isEmpty() || customer.getMobileNumber() == null || customer.getPassword() == null || customer.getPassword().isEmpty())
            return false;
        if (!isValidMobileNumber(customer.getMobileNumber()))
            return false;

        return true;
    }
    public UserDetailDTO getUserDetails(Long customerId) {
        CustomCustomer customer = em.find(CustomCustomer.class, customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found");
        }

        UserDetailDTO userDetail = new UserDetailDTO();
        userDetail.setUserId(customer.getId());
        userDetail.setUserName(customer.getFirstName());

        List<Participation> participations = em.createQuery("SELECT p FROM Participation p WHERE p.customCustomer.id = :customerId", Participation.class)
                .setParameter("customerId", customerId)
                .getResultList();

        if (!participations.isEmpty()) {
            Participation firstParticipation = participations.get(0);

            if (firstParticipation.getTeam() != null) {
                userDetail.setTeamId(firstParticipation.getTeam().getId());
                userDetail.setTeamName(firstParticipation.getTeam().getName());
            }

            if (firstParticipation.getTeam().getLeague() != null) {
                userDetail.setLeagueId(firstParticipation.getTeam().getLeague().getId());
                userDetail.setLeagueName(firstParticipation.getTeam().getLeague().getName());
            }

            userDetail.setTournaments(participations.stream()
                    .map(p -> {
                        Tournament tournament = p.getTournament();
                        TournamentDTO tournamentDTO = new TournamentDTO();
                        tournamentDTO.setId(tournament.getId());
                        tournamentDTO.setName(tournament.getName());
                        tournamentDTO.setStatus(tournament.getStatus());
                        tournamentDTO.setStartDate(tournament.getStartDate());
                        tournamentDTO.setEndDate(tournament.getEndDate());

                        List<Prize> prizes = tournamentService.getPrizesForTournament(tournament.getId());

                        List<PrizeDistributionDTO> prizeDistributionDTOs = prizes.stream()
                                .map(prize -> {
                                    PrizeDistributionDTO prizeDistributionDTO = new PrizeDistributionDTO();
                                    prizeDistributionDTO.setPrize(prize.getPrizeName());
                                    prizeDistributionDTO.setAmount(prize.getAmount());
                                    return prizeDistributionDTO;
                                })
                                .collect(Collectors.toList());

                        tournamentDTO.setPrizeDistributions(prizeDistributionDTOs.isEmpty() ? null : prizeDistributionDTOs); // Set the list of prize distributions

                        return tournamentDTO;
                    })
                    .collect(Collectors.toList()));
        }

        return userDetail;
    }




    public boolean isValidMobileNumber(String mobileNumber) {

        if (mobileNumber.startsWith("0")) {
            mobileNumber = mobileNumber.substring(1);
        }
        String mobileNumberPattern = "^\\d{9,13}$";
        return Pattern.compile(mobileNumberPattern).matcher(mobileNumber).matches();
    }

    public CustomCustomer findCustomCustomerByPhone(String mobileNumber,String countryCode) {

        if (countryCode == null) {
            countryCode = Constant.COUNTRY_CODE;
        }

        return em.createQuery(Constant.PHONE_QUERY, CustomCustomer.class)
                .setParameter("mobileNumber", mobileNumber)
                .setParameter("countryCode", countryCode)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public CustomCustomer findCustomCustomerByPhoneWithOtp(String mobileNumber,String countryCode) {

        if (countryCode == null) {
            countryCode = Constant.COUNTRY_CODE;
        }

        return em.createQuery(Constant.PHONE_QUERY_OTP, CustomCustomer.class)
                .setParameter("mobileNumber", mobileNumber)
                .setParameter("countryCode", countryCode)
                .setParameter("otp", null)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        } else {
            ipAddress = ipAddress.split(",")[0];
        }
        return ipAddress;
    }

}
