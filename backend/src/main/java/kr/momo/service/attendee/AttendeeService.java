package kr.momo.service.attendee;

import java.util.List;
import kr.momo.domain.attendee.Attendee;
import kr.momo.domain.attendee.AttendeeName;
import kr.momo.domain.attendee.AttendeePassword;
import kr.momo.domain.attendee.AttendeeRepository;
import kr.momo.domain.attendee.Role;
import kr.momo.domain.meeting.Meeting;
import kr.momo.domain.meeting.MeetingRepository;
import kr.momo.exception.MomoException;
import kr.momo.exception.code.MeetingErrorCode;
import kr.momo.service.attendee.dto.AttendeeLoginRequest;
import kr.momo.service.attendee.dto.AttendeeLoginResponse;
import kr.momo.service.auth.JwtManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttendeeService {

    private final AttendeeRepository attendeeRepository;
    private final MeetingRepository meetingRepository;
    private final JwtManager jwtManager;

    @Transactional
    public AttendeeLoginResponse login(String uuid, AttendeeLoginRequest request) {
        Meeting meeting = meetingRepository.findByUuid(uuid)
                .orElseThrow(() -> new MomoException(MeetingErrorCode.INVALID_UUID));

        AttendeeName name = new AttendeeName(request.attendeeName());
        AttendeePassword password = new AttendeePassword(request.password());

        return attendeeRepository.findByMeetingAndName(meeting, name)
                .map(attendee -> verifyPassword(attendee, password))
                .orElseGet(() -> signup(meeting, name, password));
    }

    private AttendeeLoginResponse verifyPassword(Attendee attendee, AttendeePassword password) {
        attendee.verifyPassword(password);
        return AttendeeLoginResponse.from(jwtManager.generate(attendee.getId()), attendee);
    }

    private AttendeeLoginResponse signup(Meeting meeting, AttendeeName name, AttendeePassword password) {
        Attendee attendee = new Attendee(meeting, name, password, Role.GUEST);
        attendeeRepository.save(attendee);
        return AttendeeLoginResponse.from(jwtManager.generate(attendee.getId()), attendee);
    }

    public List<String> findAll(String uuid) {
        Meeting meeting = meetingRepository.findByUuid(uuid)
                .orElseThrow(() -> new MomoException(MeetingErrorCode.INVALID_UUID));
        List<Attendee> attendees = attendeeRepository.findAllByMeeting(meeting);

        return attendees.stream()
                .map(Attendee::name)
                .toList();
    }
}
