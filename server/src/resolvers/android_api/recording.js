import { Recording, User, Poem, Bookmark } from '../../model';
import { getUser } from '../web_api/user';
import { getPoem } from '../web_api/poem';
import { createNotification } from '../web_api/notification';
import * as _ from 'lodash';
import { deleteRecordingById } from '../web_api/recording';

export const getAllRecording = async (stu_id) => {
  try {
    if (!stu_id) {
      const result = await Recording.query();
      return {
        isSuccess: true,
        recording_list: result,
      }
    }
    const user = await User.where({ stu_id, }).one();
    if (!user) {
      return {
        isSuccess: false,
        recording_list: [],
      }
    }
    const result = await Recording.where({ user_id: user.id });
    if (result.length === 0) {
      return {
        isSuccess: false,
        recording_list: [],
      }
    }
    return {
      isSuccess: true,
      recording_list: result,
    }
  } catch (err) {
    console.log('getAllRecording has err : ', err);
    return {
      isSuccess: false,
      recording_list: [],
    };
  }
}

export const getAllRecordingByLogin = async (stu_id) => {
  try {
    const user = await User.where({ stu_id, }).one();
    if (!user) {
      return [];
    }
    const result = await Recording
      .where({ user_id: user.id })
      .where({ $or: [{ auth_flag: 1 }, { auth_flag: 2 }] });
    if (result.length === 0) {
      return [];
    }
    return _.orderBy(result, ['created'], ['desc']);
  } catch (err) {
    console.log('getAllRecording has err : ', err);
    return [];
  }
}
const getRecordingForPlay = async (poem_id, user_id, isMy) => {
  try {
    if (user_id) { // 로그인
      if (isMy) { // 나의 녹음 플레이
        const recording_list = await Recording.where({ poem_id, user_id });
        return recording_list.map(async recording => {
          let isBookmarked = true;
          const bookmark = await Bookmark.where({ recording_id: recording.id, user_id });
          if (bookmark.length === 0) {
            isBookmarked = false;
          }
          return {
            isBookmarked,
            recording: recording,
          }
        });
      } else {
        const recording_list = await Recording.where({ poem_id });
        return recording_list.map(async recording => {
          let isBookmarked = true;
          const bookmark = await Bookmark.where({ recording_id: recording.id, user_id });
          if (bookmark.length === 0) {
            isBookmarked = false;
          }
          return {
            isBookmarked,
            recording: recording,
          }
        });
      }
    } else { // 비로그인
      const recording_list = await Recording.where({ poem_id });
      return recording_list.map(async recording => {
        return {
          isBookmarked: false,
          recording: recording,
        }
      });
    }
  } catch (err) {
    return [];
  }
}
const createRecording = async (input) => {
  const poem = await Poem.where({ name: input.poem_name }).include('poet');
  const poem_result = poem.filter(v => {
    if (v.poet.name === input.poet_name) {
      return true;
    }
    return false;
  })
  if (poem_result.length === 0) {
    return {
      isSuccess: false,
    }
  }
  const user = await User.where({ stu_id: input.stu_id }).one()
  const recording = new Recording({
    path: input.path,
    user_id: user.id,
    duration: input.duration,
    poem_id: poem_result[0].id,
    created: new Date(),
  });
  try {
    await recording.save();
    await createNotification({
      user_id: user.id,
      content: `${poem_result[0].name} 울림이 업로드 되었습니다.`,
    })
    return {
      isSuccess: true,
    };
  } catch (err) {
    console.log('createRecording has err : ', err);
    return {
      isSuccess: false,
    };
  }
}

const deleteRecording = async (input) => {
  const user_id = (await User.where({ stu_id: input.stu_id }).one()).id
  const poem = await Poem.where({ name: input.poem_name }).include('poet');
  const poem_result = poem.filter(v => {
    if (v.poet.name === input.poet_name) {
      return true;
    }
    return false;
  })
  if (poem_result.length === 0) {
    return {
      isSuccess: false,
    }
  }
  const recording = await Recording
    .where({ $and: [{ user_id }, { poem_id: poem_result[0].id, }] }).one();
  try {
    await deleteRecordingById(recording.id);
    return {
      isSuccess: true,
    };
  } catch (err) {
    console.log('deleteRecording has err : ', err);
    return {
      isSuccess: false,
    };
  }
}

const applyRecording = async (id_list) => {
  id_list.map(async (id) => {
    const recording = await Recording.find(id).include('poem').include('user');
    const content = `${recording.poem.name} 울림의 봉사 점수 신청이 완료되었습니다`;
    await createNotification({
      content,
      user_id: recording.user_id
    })
    const sum_point = recording.user.bongsa_time + recording.poem.point;
    await User.find(recording.user.id).update({ bongsa_time: sum_point });
    await Recording.find(id).update({ auth_flag: 2 });
  })
  return true;
}

const recordingResolver = {
  Recording: {
    user: (obj) => getUser(obj.user_id),
    poem: (obj) => getPoem(obj.poem_id),
  },
  Status: {
    REJECTED: -1,
    APPLIED: 2,
    ACCEPTED: 1,
    WAITING: 0,
  },
  Query: {
    getAllRecording: (obj, { stu_id }) => getAllRecording(stu_id),
    getRecordingForPlay: (obj, { poem_id, user_id, isMy }) => getRecordingForPlay(poem_id, user_id, isMy),
  },
  Mutation: {
    applyRecording: (obj, { id_list }) => applyRecording(id_list),
    createRecording: (obj, { input }) => createRecording(input),
    deleteRecording: (obj, { input }) => deleteRecording(input),
  }
}

export { recordingResolver };