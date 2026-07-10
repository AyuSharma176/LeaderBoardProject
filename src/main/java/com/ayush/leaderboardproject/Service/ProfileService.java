package com.ayush.leaderboardproject.Service;

import com.ayush.leaderboardproject.Model.DailyActivity;
import com.ayush.leaderboardproject.Model.LeaderBoard;
import com.ayush.leaderboardproject.Model.User;
import com.ayush.leaderboardproject.Repository.DailyActivityRepository;
import com.ayush.leaderboardproject.Repository.LeaderBoardRepository;
import com.ayush.leaderboardproject.Repository.UserRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class ProfileService {
    private final UserRepository userRepository;
    private final LeaderBoardRepository leaderBoardRepository;
    private final DailyActivityRepository dailyActivityRepository;
    private final PasswordEncoder passwordEncoder;
    private final Cloudinary cloudinary;


    public ProfileService(UserRepository userRepository, LeaderBoardRepository leaderBoardRepository, DailyActivityRepository dailyActivityRepository, PasswordEncoder passwordEncoder, Cloudinary cloudinary) {
        this.userRepository = userRepository;
        this.leaderBoardRepository = leaderBoardRepository;
        this.dailyActivityRepository = dailyActivityRepository;
        this.passwordEncoder = passwordEncoder;
        this.cloudinary = cloudinary;
    }

    public Map<String,Object> getProfile(String username){
        User user  = userRepository.findByUsername(username);
        LeaderBoard stats = leaderBoardRepository.findByLeetcodeUsername(username);
        List<DailyActivity> activity = dailyActivityRepository.findByUserOrderByDateDesc(user);

        return Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "profilePicture", user.getProfilePicture() != null
                        ? user.getProfilePicture() : "",
                "role", user.getRole(),
                "stats", stats != null ? Map.of(
                        "rank", stats.getRank(),
                        "score", stats.getScore(),
                        "solved", stats.getSolved(),
                        "easySolved", stats.getEasySolved(),
                        "mediumSolved", stats.getMediumSolved(),
                        "hardSolved", stats.getHardSolved(),
                        "dailyUnique", stats.getDailyUnique()
                ) : Map.of(),
                "activity", activity
        );
    }

    public void updateProfile(String username,String newEmail,String newPassword)throws Exception{
        User user =userRepository.findByUsername(username);

        if(newEmail!=null && !newEmail.isBlank()){
            if(userRepository.existsByEmail(newEmail) && !newEmail.equals(user.getEmail())){
                throw new Exception("Email Already Exists");
            }
            user.setEmail(newEmail);
        }

        if(newPassword!=null && !newPassword.isBlank()){
           if(newPassword.length()<6){
               throw new Exception("Password must contains atleast 6 Characters");
           }
           user.setPassword(passwordEncoder.encode(newPassword));
        }
        userRepository.save(user);
    }

    public String uploadProfilePicture(String username, MultipartFile file) throws IOException {
        try {
            User user = userRepository.findByUsername(username);

            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "leaderboard/avatars",
                            "public_id", "avatar_" + username,
                            "overwrite", true
                    )
            );

            String imageUrl = (String) uploadResult.get("secure_url");
            user.setProfilePicture(imageUrl);
            userRepository.save(user);
            return imageUrl;

        } catch (Exception e) {
            System.err.println("Cloudinary upload error: " + e.getMessage());
            throw new IOException(e.getMessage());
        }
    }
    public  void recordDailyActivity(User user, int solvedToday) {
        LocalDate today = LocalDate.now();

        dailyActivityRepository.findByUserAndDate(user, today)
                .ifPresentOrElse(
                        existing -> {
                            existing.setSolved(solvedToday);
                            dailyActivityRepository.save(existing);
                        },
                        () -> dailyActivityRepository.save(
                                DailyActivity.builder()
                                        .user(user)
                                        .date(today)
                                        .solved(solvedToday)
                                        .build()
                        )
                );
    }
}
