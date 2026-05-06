package com.inchinso.back.domain.user.service;

import com.inchinso.back.domain.attendance.repository.AttendanceRepository;
import com.inchinso.back.domain.participation.entity.ParticipationStatus;
import com.inchinso.back.domain.participation.repository.ParticipationRepository;
import com.inchinso.back.domain.user.entity.Role;
import com.inchinso.back.domain.user.entity.User;
import com.inchinso.back.domain.user.repository.UserRepository;
import com.inchinso.back.global.exception.CustomException;
import com.inchinso.back.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ParticipationRepository participationRepository;
    private final AttendanceRepository attendanceRepository;

    /** 내 정보 조회 */
    @Transactional(readOnly = true)
    public MyInfoResponse getMyInfo(Long userId) {
        User user = getUser(userId);
        int totalCount = participationRepository.countTotalByUserId(userId);
        LocalDateTime firstOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        int monthlyCount = attendanceRepository.countByUserIdAndAttendedAtAfter(userId, firstOfMonth);

        return new MyInfoResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getProfileImageUrl(),
                totalCount,
                monthlyCount
        );
    }

    /** 전체 회원 목록 조회 (운영진 전용) */
    @Transactional(readOnly = true)
    public List<MemberResponse> getAllMembers() {
        return userRepository.findAll().stream()
                .filter(User::isActive)
                .map(u -> new MemberResponse(
                        u.getId(),
                        u.getName(),
                        u.getEmail(),
                        u.getRole().name(),
                        participationRepository.countTotalByUserId(u.getId())
                ))
                .toList();
    }

    /** 회원 삭제 (비활성화) - 운영진 전용 */
    @Transactional
    public void deactivateUser(Long adminId, Long targetUserId) {
        if (adminId.equals(targetUserId)) {
            throw new CustomException(ErrorCode.CANNOT_DEACTIVATE_SELF);
        }
        User target = getUser(targetUserId);
        target.deactivate();
    }

    /** 권한 승계 - 운영진 전용 */
    @Transactional
    public void transferAdmin(Long currentAdminId, Long targetUserId) {
        User currentAdmin = getUser(currentAdminId);
        User target = getUser(targetUserId);
        currentAdmin.updateRole(Role.USER);
        target.updateRole(Role.ADMIN);
    }

    /** 특정 회원에게 운영진 권한 부여 */
    @Transactional
    public void grantAdmin(Long targetUserId) {
        User target = getUser(targetUserId);
        target.updateRole(Role.ADMIN);
    }

    /** 특정 회원 운영진 해제 */
    @Transactional
    public void revokeAdmin(Long targetUserId) {
        User target = getUser(targetUserId);
        target.updateRole(Role.USER);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    public record MyInfoResponse(
            Long id, String name, String email, String role,
            String profileImageUrl, int totalParticipations, int monthlyParticipations
    ) {}

    public record MemberResponse(
            Long id, String name, String email, String role, int totalParticipations
    ) {}
}
