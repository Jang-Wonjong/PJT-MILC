package com.jpmp.api.service.user;

import com.jpmp.api.dto.TokenDto;
import com.jpmp.api.dto.request.user.UserLoginReqDto;
import com.jpmp.api.dto.request.user.UserModifyReqDto;
import com.jpmp.api.dto.request.user.UserRegisterReqDto;
import com.jpmp.common.auth.JwtExpirationEnums;
import com.jpmp.common.util.JwtTokenUtil;
import com.jpmp.common.util.RefreshToken;
import com.jpmp.db.entity.user.User;
import com.jpmp.db.repository.jwt.RefreshTokenRedisRepository;
import com.jpmp.db.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.NoSuchElementException;

import static com.jpmp.common.auth.JwtExpirationEnums.REFRESH_TOKEN_EXPIRATION_TIME;
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;
    @Override
    public User createUser(UserRegisterReqDto registerRequestDto) {
        // 이게 좀더 맞는 코딩!
        // 나중에 중복확인 추가
        registerRequestDto.setPassword(passwordEncoder.encode(registerRequestDto.getPassword()));
        return userRepository.save(User.ofUser(registerRequestDto));
    }

    @Override
    public User getUserByEmail(String email) {

        return userRepository.findByEmail(email).get();
    }

    @Override
    public TokenDto login(UserLoginReqDto userLoginReqDto) {

        User user = userRepository.findByEmail(userLoginReqDto.getEmail()).orElseThrow(() -> new NoSuchElementException("회원이 없습니다."));
        checkPassword(userLoginReqDto.getPassword(), user.getPassword());

        String username = user.getUsername();
        String accessToken = jwtTokenUtil.generateAccessToken(username);
        RefreshToken refreshToken = saveRefreshToken(username);

        return  TokenDto.of(accessToken, refreshToken.getRefreshToken() , user);
    }
    private void checkPassword(String rawPassword, String findMemberPassword) {
        if (!passwordEncoder.matches(rawPassword, findMemberPassword)) {
            throw new IllegalArgumentException("비밀번호가 맞지 않습니다.");
        }
    }
    private RefreshToken saveRefreshToken(String username) {
        return refreshTokenRedisRepository.save(RefreshToken.createRefreshToken(username,
                jwtTokenUtil.generateRefreshToken(username), REFRESH_TOKEN_EXPIRATION_TIME.getValue()));
    }

    @Override// 이거 왜 drity check 안댐?
    public User modifyUser(User user, UserModifyReqDto userModifyReqDto) {
        user.changeUser(userModifyReqDto);// 이거 동적으로는 안되나?
        userRepository.save(user);
        return user;
    }

    @Override// 이거 왜 drity check 안댐?
    public User modifyProImgUser(User user, String proFileImg) {
        user.changeProfileImg(proFileImg);
        userRepository.save(user);
        return user;
    }

    @Override// 이거 왜 drity check 안댐?
    public User modifyBackImgUser(User user, String backFileImg) {
        user.changeBackgroundfileImg(backFileImg);
        userRepository.save(user);
        return user;
    }
    @Override
    @Transactional
    public User addUserNftLike(User userDetails, String nftId) {
        userDetails.getLikeList().add(nftId);// 이렇게 하면 못읽음 1:N 일떄 주체 인쪽에서 수정해야지!
        userRepository.save(userDetails);
        // 나중에 물어보기
//        List<String> likeList = userDetails.getLikeList();
//        likeList.add(nftId);

        return userDetails;
    }

    @Override
    public User deleteUserNftLike(User userDetails, String nftId) {
        userDetails.getLikeList().remove(nftId);
        userRepository.save(userDetails);
        return userDetails;
    }

    @Override
    public TokenDto reissue(String refreshToken, String username) {


//        RefreshToken redisRefreshToken = refreshTokenRedisRepository.findById(username).orElseThrow(NoSuchElementException::new);

//        if (refreshToken.equals(redisRefreshToken.getRefreshToken())) {
            return reissueRefreshToken(refreshToken, username);
//        }
//        throw new IllegalArgumentException("토큰이 일치하지 않습니다.");
//
    }

    private TokenDto reissueRefreshToken(String refreshToken, String username) {
        if (lessThanReissueExpirationTimesLeft(refreshToken)) {
            String accessToken = jwtTokenUtil.generateAccessToken(username);
            return TokenDto.of(accessToken, saveRefreshToken(username).getRefreshToken());
        }
        return TokenDto.of(jwtTokenUtil.generateAccessToken(username), refreshToken);
    }
    private boolean lessThanReissueExpirationTimesLeft(String refreshToken) {
        return jwtTokenUtil.getRemainMilliSeconds(refreshToken) < JwtExpirationEnums.REISSUE_EXPIRATION_TIME.getValue();
    }
    @Override
    @Transactional(readOnly = true)
    public Boolean checkDuplicateNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }


}
