package com.bot.individualentrepreneurbot.service;

import com.bot.individualentrepreneurbot.DocumentHandler;
import com.bot.individualentrepreneurbot.config.BotConfig;
import com.bot.individualentrepreneurbot.dao.CompanyDao;
import com.sun.research.ws.wadl.Doc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    @Autowired
    private DocumentHandler documentHandler;
    @Autowired
    private CompanyDao companyDao;
    private final BotConfig config;
    private boolean is_now_choose_company = false;

    public TelegramBot(BotConfig config) {
        this.config = config;
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            try {
                if (messageText.equals("/start")) {
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                } else if (is_now_choose_company) {
                    if(messageText.equals("1")){
                        sendMessage(chatId, companyDao.findByName("aaa").getRequisites());
                    } else if(messageText.equals("2")){

                    } else {
                        throw new TelegramApiException();
                    }
                    is_now_choose_company = false;
                } else  {
                    sendMessage(chatId, "Не поддерживается");
                }

//                    case "/getDoc" -> sendDocument(chatId, new File(documentHandler.getInputFileName()));

            } catch (TelegramApiException e) {
                sendMessage(chatId, "Что-то пошло не так. Попробуйте снова с /start");
            }
        }
    }

    private void startCommandReceived(long chatId, String firstName) throws TelegramApiException {
        String answer = "Здравствуйте, " + firstName + ". Что вы хотите сделать?\n" + "<b>1-Выбрать компанию из имеющихся</b>\n" + "<b>2-Добавить компанию</b>";
        sendMessage(chatId, answer);
        is_now_choose_company = true;
    }

    private void sendDocument(Long chatId, File save) throws TelegramApiException {
        System.out.println(save.getAbsolutePath());
        SendDocument sendDocumentRequest = new SendDocument();
        sendDocumentRequest.setChatId(chatId + "");
        sendDocumentRequest.setDocument(new InputFile(save));
        execute(sendDocumentRequest);
    }

    private void sendMessage(long chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(message);
        sendMessage.enableHtml(true);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
