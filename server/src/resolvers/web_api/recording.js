import { Recording, Poem } from '../../model';
import { getUser } from './user';
import { getPoem } from './poem';
import { createNotification } from './notification';
import * as _ from 'lodash';
import path from 'path';
import fs from 'fs';

const storage_path = '../../../../../woolrim_storage/';

const deleteRecodingFile = (recording_path) => {
  const file_path = path.join(__dirname, storage_path + recording_path);
  if (!fs.existsSync(file_path)){
    console.log(`error while deleting ${recording_path}: not exist`);
    return false;
  }
  try{
    fs.unlinkSync(file_path);
    return true;
  }catch(err){
    console.log(`error while deleting ${recording_path}: unknown error`);
    return false;
  }
}

export const getRecording = async (id) => {
  return await Recording.find(id);
}

export const deleteRecordingById = async (id) => {
  const recording = await Recording.find(id).include('user').include('poem');
  if(recording.auth_flag === 2){
    return false;
  }
  await Recording.find(id).delete();
  deleteRecodingFile(recording.path);
  if(recording.auth_flag !== 1){
    return true;
  }
  if (recording.user.gender === '남자') {
    await Poem.find(recording.poem.id).update({ auth_count_man: recording.poem.auth_count_man - 1 });
  } else {
    await Poem.find(recording.poem.id).update({ auth_count_woman: recording.poem.auth_count_woman - 1 });
  }
  return true;
}

const acceptRecording = async (recording_id) => {
  const recording = await Recording.find(recording_id).include('user').include('poem');
  if (recording.user.gender === '남자') {
    if (recording.poem.auth_count_man + 1 > recording.poem.auth_count / 2) {
      return false;
    }
    await Poem.find(recording.poem.id).update({ auth_count_man: recording.poem.auth_count_man + 1 });
  } else {
    if (recording.poem.auth_count_woman + 1 > recording.poem.auth_count / 2) {
      return false;
    }
    await Poem.find(recording.poem.id).update({ auth_count_woman: recording.poem.auth_count_woman + 1 });
  }
  await Recording.find(recording_id).update({ auth_flag: 1 });
  await createNotification({
    user_id: recording.user.id,
    content: `${recording.poem.name} 울림이 승인되었습니다.`
  })
  return true;
}

const rejectRecording = async (recording_id) => {
  await Recording.find(recording_id).update({ auth_flag: -1 });
  const recording = await Recording.find(recording_id).include('user').include('poem');
  await deleteRecordingById(recording.id);
  await createNotification({
    user_id: recording.user.id,
    content: `${recording.poem.name} 울림이 거절되었습니다.`
  })
  return true;
}

const getAllRecordingForAudit = async () => {
  const recording_list = await Recording.where({ auth_flag: 0 }).include('user');
  const user_list = _.uniqBy(recording_list, 'user_id');
  const result_promise = user_list.map(async item => {
    const recording_list = await Recording.where({ auth_flag: 0, user_id: item.user_id });
    return {
      user: item.user,
      recording_list,
    }
  })
  return await Promise.all(result_promise);
}

const recordingWebResolver = {
  Recording: {
    user: (obj) => getUser(obj.user_id),
    poem: (obj) => getPoem(obj.poem_id),
  },
  Status: {
    REJECTED: -1,
    ACCEPTED: 1,
    WAITING: 0,
  },
  Query: {
    getRecording: (obj, { id }) => getRecording(id),
    getAllRecordingForAudit: () => getAllRecordingForAudit(),
  },
  Mutation: {
    acceptRecording: (obj, { recording_id }) => acceptRecording(recording_id),
    rejectRecording: (obj, { recording_id }) => rejectRecording(recording_id),
    deleteRecordingById: (obj, { id }) => deleteRecordingById(id),
  }
}

export { recordingWebResolver };