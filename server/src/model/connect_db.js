import { Connection } from 'cormo';

// export const Connection = new cormo.Connection('mysql', { 
//     host: 'woolrimdb.cbb7lmv7tjj3.ap-northeast-2.rds.amazonaws.com',
//     port: 3306,
//     user: 'whtnrms2018',
//     password: 'dnfflaelql',
//     database: 'woolrim_proj',
// });
export const Conn = new Connection('mysql', {
  host: 'localhost',
  port: 3306,
  user: 'root',
  password: 'Tnrms@@852',
  database: 'woolrim',
});
