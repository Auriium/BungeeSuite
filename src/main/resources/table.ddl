

CREATE TABLE IF NOT EXISTS player_info
  (
    id          CHAR(36),
    nickname    VARCHAR(24),
    discord_in     BOOLEAN,
    discord_out    BOOLEAN,
    pms         BOOLEAN,
    chat_color  VARCHAR(24),
    PRIMARY KEY (id)
  );

CREATE TABLE IF NOT EXISTS player_login
  (
     id         CHAR(36),
     name       VARCHAR(16),
     ip_address INT UNSIGNED NOT NULL,
     time       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
  );

CREATE OR REPLACE view player_latest_login
AS
  SELECT id, name, ip_address, time
  from (
         SELECT player_login.*, ROW_NUMBER() OVER (PARTITION BY id ORDER BY time DESC) AS rn
         FROM player_login
       ) m2
  where m2.rn = 1;

CREATE OR REPLACE view player_related_ip_login
AS
  SELECT DISTINCT l1.id   id1,
                  l2.id   id2,
                  l1.name name1,
                  l2.name name2,
                  l1.time time1,
                  l2.time time2
  FROM   player_login l1
         JOIN player_login l2
           ON l1.ip_address = l2.ip_address
              AND l1.id <> l2.id
;

CREATE TABLE IF NOT EXISTS player_ip_ban
  (
     id            INT auto_increment,
     ip_address    INT UNSIGNED,
     sender_id     CHAR(36),
     reason        VARCHAR(100) NOT NULL,
     creation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     expiry_date   TIMESTAMP NULL,
     PRIMARY KEY (id)
  );

CREATE TABLE IF NOT EXISTS player_ip_unban
  (
     ban_id        INT,
     ip_address    INT UNSIGNED,
     sender_id     CHAR(36),
     creation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     FOREIGN KEY (ban_id) REFERENCES player_ip_ban (id) ON DELETE CASCADE
  );

CREATE OR REPLACE view player_active_ip_ban
AS
  SELECT id,
         Inet_ntoa(ip_address) ip_address,
         sender_id,
         reason,
         creation_date,
         expiry_date
  FROM   player_ip_ban
  WHERE  expiry_date > CURRENT_TIMESTAMP
         AND id NOT IN (SELECT ban_id
                          FROM   player_ip_unban
                          WHERE  player_ip_unban.ban_id = player_ip_ban.id)
  ;

CREATE TABLE IF NOT EXISTS player_punish
  (
     id            INT auto_increment,
     banned_id     CHAR(36),
     sender_id     CHAR(36),
     reason        VARCHAR(100) NOT NULL,
     creation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     expiry_date   TIMESTAMP NULL,
     type          ENUM ('ban', 'mute', 'warn', 'kick'),
     PRIMARY KEY (id)
  );

CREATE TABLE IF NOT EXISTS player_punish_reverse
  (
     punish_id     INT NOT NULL,
     banned_id     CHAR(36) NOT NULL,
     sender_id     CHAR(36) NULL,
     reason        VARCHAR(100),
     creation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     FOREIGN KEY (punish_id) REFERENCES player_punish (id) ON DELETE
     CASCADE
  );

CREATE OR REPLACE view player_active_punishment
AS
  SELECT *
  FROM   player_punish
  WHERE  ( expiry_date > CURRENT_TIMESTAMP
            OR expiry_date IS NULL )
         AND id NOT IN (SELECT punish_id
                          FROM   player_punish_reverse
                          WHERE  player_punish_reverse.punish_id =
                                 player_punish.id);

CREATE OR REPLACE view player_combined_info
AS
  SELECT id, name, nickname, discord_in, discord_out, pms, chat_color, level, experience, money, unlocked_rewards
  from (
         SELECT player_login.id, player_login.name, player_info.nickname, player_info.discord_in, player_info.discord_out, player_info.pms, player_info.chat_color, levels_player.level, levels_player.experience, levels_player.money, levels_player.unlocked_rewards, ROW_NUMBER() OVER (PARTITION BY id ORDER BY time DESC) AS rn
         FROM ((player_info
                   INNER JOIN player_login ON player_info.id = player_login.id)
                   INNER JOIN levels_player ON player_info.id = levels_player.player_uuid)
       ) m2
  where m2.rn = 1;


