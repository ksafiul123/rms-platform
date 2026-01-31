-- V12__create_waiting_time_engagement_system.sql

-- Game Types
CREATE TABLE game_types (
                            id BIGSERIAL PRIMARY KEY,
                            name VARCHAR(100) NOT NULL UNIQUE,
                            code VARCHAR(50) NOT NULL UNIQUE,
                            description TEXT,
                            game_mode VARCHAR(30) NOT NULL,
                            min_players INTEGER NOT NULL DEFAULT 1,
                            max_players INTEGER NOT NULL DEFAULT 4,
                            average_duration_seconds INTEGER NOT NULL,
                            base_points_per_game INTEGER NOT NULL DEFAULT 100,
                            difficulty_level INTEGER DEFAULT 1,
                            is_active BOOLEAN NOT NULL DEFAULT TRUE,
                            icon_url VARCHAR(500),
                            thumbnail_url VARCHAR(500),
                            config_json TEXT,
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                            CONSTRAINT chk_game_mode CHECK (game_mode IN (
                                                                          'SINGLE_PLAYER', 'MULTIPLAYER_COMPETITIVE', 'MULTIPLAYER_COOPERATIVE',
                                                                          'TEAM_BASED', 'TURN_BASED', 'REAL_TIME'
                                )),
                            CONSTRAINT chk_difficulty CHECK (difficulty_level BETWEEN 1 AND 5)
);

CREATE INDEX idx_game_type_active ON game_types(is_active);
CREATE INDEX idx_game_type_code ON game_types(code);

-- Game Sessions
CREATE TABLE game_sessions (
                               id BIGSERIAL PRIMARY KEY,
                               table_session_id BIGINT NOT NULL REFERENCES table_sessions(id) ON DELETE CASCADE,
                               game_type_id BIGINT NOT NULL REFERENCES game_types(id),
                               restaurant_id BIGINT NOT NULL REFERENCES restaurants(id),
                               session_code VARCHAR(20) NOT NULL UNIQUE,
                               status VARCHAR(20) NOT NULL,
                               started_at TIMESTAMP,
                               ended_at TIMESTAMP,
                               duration_seconds INTEGER,
                               total_rounds INTEGER NOT NULL DEFAULT 1,
                               current_round INTEGER NOT NULL DEFAULT 1,
                               player_count INTEGER NOT NULL DEFAULT 0,
                               winner_id BIGINT,
                               total_points_awarded INTEGER DEFAULT 0,
                               discount_earned DECIMAL(10,2) DEFAULT 0,
                               game_data TEXT,
                               completed_successfully BOOLEAN NOT NULL DEFAULT FALSE,
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT chk_session_status CHECK (status IN (
                                                                               'WAITING', 'IN_PROGRESS', 'PAUSED', 'COMPLETED', 'ABANDONED', 'EXPIRED'
                                   ))
);

CREATE INDEX idx_game_session_table ON game_sessions(table_session_id);
CREATE INDEX idx_game_session_status ON game_sessions(status);
CREATE INDEX idx_game_session_started ON game_sessions(started_at);
CREATE INDEX idx_game_session_restaurant ON game_sessions(restaurant_id);

-- Game Players
CREATE TABLE game_players (
                              id BIGSERIAL PRIMARY KEY,
                              game_session_id BIGINT NOT NULL REFERENCES game_sessions(id) ON DELETE CASCADE,
                              user_id BIGINT REFERENCES users(id),
                              player_name VARCHAR(100) NOT NULL,
                              guest_identifier VARCHAR(100),
                              team_id INTEGER,
                              joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              left_at TIMESTAMP,
                              is_active BOOLEAN NOT NULL DEFAULT TRUE,
                              current_score INTEGER NOT NULL DEFAULT 0,
                              final_score INTEGER,
                              rank INTEGER,
                              accuracy_percentage DECIMAL(5,2),
                              completion_time_seconds INTEGER,
                              correct_answers INTEGER DEFAULT 0,
                              wrong_answers INTEGER DEFAULT 0,
                              bonus_points INTEGER DEFAULT 0,
                              combo_multiplier DECIMAL(5,2) DEFAULT 1.00,
                              achievements_earned TEXT,
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_game_player_session ON game_players(game_session_id);
CREATE INDEX idx_game_player_user ON game_players(user_id);
CREATE INDEX idx_game_player_score ON game_players(final_score);

-- Game Scores
CREATE TABLE game_scores (
                             id BIGSERIAL PRIMARY KEY,
                             game_player_id BIGINT NOT NULL REFERENCES game_players(id) ON DELETE CASCADE,
                             score_type VARCHAR(30) NOT NULL,
                             points INTEGER NOT NULL,
                             round_number INTEGER,
                             scored_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             description VARCHAR(200),
                             metadata TEXT,
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                             CONSTRAINT chk_score_type CHECK (score_type IN (
                                                                             'CORRECT_ANSWER', 'WRONG_ANSWER', 'TIME_BONUS', 'ACCURACY_BONUS',
                                                                             'STREAK_BONUS', 'COMBO_MULTIPLIER', 'COMPLETION_BONUS',
                                                                             'PERFECT_ROUND', 'SPEED_BONUS', 'TEAM_CONTRIBUTION'
                                 ))
);

CREATE INDEX idx_game_score_player ON game_scores(game_player_id);
CREATE INDEX idx_game_score_timestamp ON game_scores(scored_at);

-- Discount Rewards
CREATE TABLE discount_rewards (
                                  id BIGSERIAL PRIMARY KEY,
                                  user_id BIGINT NOT NULL REFERENCES users(id),
                                  game_session_id BIGINT NOT NULL REFERENCES game_sessions(id),
                                  order_id BIGINT REFERENCES orders(id),
                                  restaurant_id BIGINT NOT NULL REFERENCES restaurants(id),
                                  reward_code VARCHAR(20) NOT NULL UNIQUE,
                                  reward_type VARCHAR(30) NOT NULL,
                                  discount_percentage DECIMAL(5,2),
                                  discount_amount DECIMAL(10,2),
                                  max_discount_amount DECIMAL(10,2),
                                  min_order_amount DECIMAL(10,2),
                                  points_earned INTEGER NOT NULL,
                                  earned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  expires_at TIMESTAMP,
                                  applied_at TIMESTAMP,
                                  status VARCHAR(20) NOT NULL,
                                  description VARCHAR(500),
                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT chk_reward_type CHECK (reward_type IN (
                                                                                    'PERCENTAGE_DISCOUNT', 'FIXED_AMOUNT', 'FREE_ITEM',
                                                                                    'UPGRADE', 'BONUS_POINTS', 'NEXT_ORDER_DISCOUNT'
                                      )),
                                  CONSTRAINT chk_reward_status CHECK (status IN (
                                                                                 'ACTIVE', 'APPLIED', 'EXPIRED', 'CANCELLED'
                                      ))
);

CREATE INDEX idx_discount_user ON discount_rewards(user_id);
CREATE INDEX idx_discount_order ON discount_rewards(order_id);
CREATE INDEX idx_discount_status ON discount_rewards(status);
CREATE INDEX idx_discount_expires ON discount_rewards(expires_at);

-- Game Leaderboards
CREATE TABLE game_leaderboards (
                                   id BIGSERIAL PRIMARY KEY,
                                   user_id BIGINT NOT NULL REFERENCES users(id),
                                   restaurant_id BIGINT NOT NULL REFERENCES restaurants(id),
                                   period_type VARCHAR(20) NOT NULL,
                                   period_date DATE NOT NULL,
                                   total_points INTEGER NOT NULL DEFAULT 0,
                                   total_games_played INTEGER NOT NULL DEFAULT 0,
                                   games_won INTEGER NOT NULL DEFAULT 0,
                                   win_rate DECIMAL(5,2),
                                   rank INTEGER NOT NULL DEFAULT 0,
                                   average_score DECIMAL(10,2),
                                   highest_score INTEGER DEFAULT 0,
                                   total_discount_earned DECIMAL(10,2) DEFAULT 0,
                                   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                   CONSTRAINT chk_period_type CHECK (period_type IN (
                                                                                     'DAILY', 'WEEKLY', 'MONTHLY', 'ALL_TIME'
                                       )),
                                   CONSTRAINT uq_leaderboard_entry UNIQUE (user_id, restaurant_id, period_type, period_date)
);

CREATE INDEX idx_leaderboard_period ON game_leaderboards(period_type, period_date);
CREATE INDEX idx_leaderboard_rank ON game_leaderboards(rank);
CREATE INDEX idx_leaderboard_user ON game_leaderboards(user_id);

-- Player Achievements
CREATE TABLE player_achievements (
                                     id BIGSERIAL PRIMARY KEY,
                                     user_id BIGINT NOT NULL REFERENCES users(id),
                                     achievement_type VARCHAR(30) NOT NULL,
                                     name VARCHAR(100) NOT NULL,
                                     description VARCHAR(500),
                                     points_awarded INTEGER NOT NULL,
                                     earned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     progress_value INTEGER,
                                     icon_url VARCHAR(500),
                                     badge_tier VARCHAR(20),
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                     CONSTRAINT chk_achievement_type CHECK (achievement_type IN (
                                                                                                 'FIRST_GAME', 'FIRST_WIN', 'PERFECT_SCORE', 'SPEED_DEMON',
                                                                                                 'ACCURACY_MASTER', 'STREAK_CHAMPION', 'SOCIAL_BUTTERFLY',
                                                                                                 'GAME_VETERAN', 'POINT_MILESTONE', 'DISCOUNT_HUNTER',
                                                                                                 'DAILY_PLAYER', 'GAME_MASTER'
                                         )),
                                     CONSTRAINT uq_user_achievement UNIQUE (user_id, achievement_type)
);

CREATE INDEX idx_achievement_user ON player_achievements(user_id);
CREATE INDEX idx_achievement_type ON player_achievements(achievement_type);

-- Seed game types
INSERT INTO game_types (name, code, description, game_mode, min_players, max_players, average_duration_seconds, base_points_per_game, difficulty_level) VALUES
                                                                                                                                                            ('Trivia Quiz', 'TRIVIA_QUIZ', 'Answer trivia questions to earn points', 'MULTIPLAYER_COMPETITIVE', 1, 4, 300, 100, 2),
                                                                                                                                                            ('Memory Match', 'MEMORY_MATCH', 'Match pairs of cards', 'SINGLE_PLAYER', 1, 2, 180, 80, 1),
                                                                                                                                                            ('Quick Draw', 'QUICK_DRAW', 'Draw and guess', 'MULTIPLAYER_COOPERATIVE', 2, 4, 240, 120, 2),
                                                                                                                                                            ('Word Puzzle', 'WORD_PUZZLE', 'Solve word puzzles', 'SINGLE_PLAYER', 1, 4, 300, 90, 3),
                                                                                                                                                            ('Math Challenge', 'MATH_CHALLENGE', 'Solve math problems quickly', 'MULTIPLAYER_COMPETITIVE', 1, 4, 180, 100, 2);

-- Analytical views
CREATE OR REPLACE VIEW game_engagement_stats AS
SELECT
    DATE(gs.started_at) as play_date,
    r.id as restaurant_id,
    r.name as restaurant_name,
    COUNT(DISTINCT gs.id) as total_games,
    COUNT(DISTINCT gp.id) as total_players,
    AVG(gs.duration_seconds) as avg_duration,
    AVG(gs.total_points_awarded) as avg_points,
    SUM(gs.discount_earned) as total_discounts_earned
FROM game_sessions gs
         JOIN restaurants r ON gs.restaurant_id = r.id
         LEFT JOIN game_players gp ON gs.id = gp.game_session_id
WHERE gs.status = 'COMPLETED'
GROUP BY DATE(gs.started_at), r.id, r.name;

CREATE OR REPLACE VIEW top_players_all_time AS
SELECT
    u.id as user_id,
    u.full_name,
    u.email,
    COUNT(DISTINCT gp.game_session_id) as games_played,
    SUM(gp.final_score) as total_score,
    AVG(gp.final_score) as avg_score,
    SUM(CASE WHEN gp.rank = 1 THEN 1 ELSE 0 END) as wins,
    SUM(dr.discount_amount) as total_discounts
FROM users u
         LEFT JOIN game_players gp ON u.id = gp.user_id
         LEFT JOIN discount_rewards dr ON u.id = dr.user_id AND dr.status = 'APPLIED'
GROUP BY u.id, u.full_name, u.email
ORDER BY total_score DESC
LIMIT 100;

CREATE OR REPLACE VIEW active_rewards_summary AS
SELECT
    u.id as user_id,
    u.full_name,
    COUNT(*) as active_reward_count,
    SUM(dr.discount_percentage) as total_discount_percentage,
    MIN(dr.expires_at) as next_expiry
FROM users u
         JOIN discount_rewards dr ON u.id = dr.user_id
WHERE dr.status = 'ACTIVE' AND dr.expires_at > CURRENT_TIMESTAMP
GROUP BY u.id, u.full_name;

-- Scheduled job function to expire old rewards
CREATE OR REPLACE FUNCTION expire_old_rewards()
    RETURNS void AS $$
BEGIN
    UPDATE discount_rewards
    SET status = 'EXPIRED'
    WHERE status = 'ACTIVE'
      AND expires_at <= CURRENT_TIMESTAMP;
END;
$$ LANGUAGE plpgsql;

COMMENT ON TABLE game_types IS 'Available game types customers can play';
COMMENT ON TABLE game_sessions IS 'Game sessions linked to table sessions';
COMMENT ON TABLE game_players IS 'Individual player participation in games';
COMMENT ON TABLE game_scores IS 'Detailed score tracking per player';
COMMENT ON TABLE discount_rewards IS 'Rewards earned from gameplay';
COMMENT ON TABLE game_leaderboards IS 'Player rankings and statistics';
COMMENT ON TABLE player_achievements IS 'Earned achievements and badges';