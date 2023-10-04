INSERT INTO AbstractTask(id, taskType) VALUES(0, 1);
INSERT INTO AbstractTaskText(id, locale, content) VALUES(0, 'en', 'This is a pub mode');
INSERT INTO LocaleTaskText(taskText_id, taskText_locale, locale, content) VALUES(0, 'en', 'sk', 'Toto je pub mód');