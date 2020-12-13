
CREATE TABLE IF NOT EXISTS players
  (
    id            CHAR(36) NOT NULL UNIQUE,
    name          VARCHAR(24),
    nickname      VARCHAR(24),
    PRIMARY KEY (id)
  );

CREATE TABLE IF NOT EXISTS players_levels
  (
    foreign_id    CHAR(36),
    level         INT(4),
    experience    INT(4),
    money         INT(4),
    unlocked_rewards    VARCHAR(100) NOT NULL,
    FOREIGN KEY (foreign_id) REFERENCES players (id) ON DELETE CASCADE
  );

CREATE TABLE IF NOT EXISTS player_settings
  (
    foreign_id     CHAR(36),
    discord_in     BOOLEAN,
    discord_out    BOOLEAN,
    pms            BOOLEAN,
    chat_color     VARCHAR(24),
    chat_enabled   BOOLEAN,
    ignored_players  VARCHAR(100) NOT NULL,
    FOREIGN KEY (foreign_id) REFERENCES players (id) ON DELETE CASCADE
  );

CREATE TABLE IF NOT EXISTS player_login
  (
     foreign_id      CHAR(36),
     name            VARCHAR(16),
     ip_address      INT UNSIGNED NOT NULL,
     time            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     FOREIGN KEY (foreign_id) REFERENCES players (id) ON DELETE CASCADE
  );

CREATE OR REPLACE view player_latest_login
AS
  SELECT foreign_id, name, ip_address, time
  from (
         SELECT player_login.*, ROW_NUMBER() OVER (PARTITION BY foreign_id ORDER BY time DESC) AS rn
         FROM player_login
       ) m2
  where m2.rn = 1;

CREATE OR REPLACE view player_related_ip_login
AS
  SELECT DISTINCT l1.foreign_id   id1,
                  l2.foreign_id   id2,
                  l1.name name1,
                  l2.name name2,
                  l1.time time1,
                  l2.time time2
  FROM   player_login l1
         JOIN player_login l2
           ON l1.ip_address = l2.ip_address
              AND l1.foreign_id <> l2.foreign_id
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
   SELECT players.id, players.name, players.nickname, player_settings.discord_in,
   player_settings.discord_out, player_settings.pms, player_settings.chat_color,
   player_settings.chat_enabled, player_settings.ignored_players, players_levels.level, players_levels.experience,
   players_levels.money, players_levels.unlocked_rewards

   FROM ((players
         INNER JOIN player_settings ON players.id = player_settings.foreign_id)
         INNER JOIN players_levels ON players.id = players_levels.foreign_id);


