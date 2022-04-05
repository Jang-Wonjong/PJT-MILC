package com.jpmp.api.dto.response.live;

import com.jpmp.api.dto.request.live.LiveDto;
import com.jpmp.api.dto.response.BaseResponseBody;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@Getter
@Setter
@ToString
public class LiveListResDto extends BaseResponseBody {

    private List<LiveDto> LiveDtoList;

    public static LiveListResDto of(Integer statusCode, String message, List<LiveDto> liveList) {
        LiveListResDto res = new LiveListResDto();
        res.setStatusCode(statusCode);
        res.setMessage(message);
        res.setLiveDtoList(liveList);
        return res;
    }

    public static LiveListResDto of(Integer statusCode, String message, LiveDto liveDto) {
        LiveListResDto res = new LiveListResDto();
        res.setStatusCode(statusCode);
        res.setMessage(message);
        res.setLiveDto(liveDto);
        return res;
    }

    public void setLiveDtoList(List<LiveDto> liveList) {
        LiveDtoList = new ArrayList<>();
        for (int i =0 ; i < liveList.size() ; i ++){
            LiveDtoList.add(liveList.get(i));
        }
    }

    public void setLiveDto(LiveDto liveDto) {
        LiveDtoList = new ArrayList<>();
        LiveDtoList.add(liveDto);
    }
}
