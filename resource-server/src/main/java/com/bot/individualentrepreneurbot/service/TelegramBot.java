package com.bot.individualentrepreneurbot.service;

import com.bot.individualentrepreneurbot.DocumentHandler;
import com.bot.individualentrepreneurbot.config.BotConfig;
import com.bot.individualentrepreneurbot.dao.Company;
import com.bot.individualentrepreneurbot.dao.CompanyDaoHandler;
import jakarta.ws.rs.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    @Autowired
    private DocumentHandler documentHandler;
    @Autowired
    private CompanyDaoHandler companyDaoHandler;
    private final BotConfig config;
    private LastOperation lastOperation = LastOperation.START;
    private String[] inputData;

    private static final String YES_BUTTON = "YES_BUTTON";
    private static final String NO_BUTTON = "NO_BUTTON";

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
                    } else if (lastOperation == LastOperation.CHOOSING_COMPANIES) {
                        if (messageText.equals("1")) {
                            showAllCompanies(chatId);
                        } else if (messageText.equals("2")) {

                        } else {
                            lastOperation = LastOperation.START;
                            throw new TelegramApiException();
                        }
                    } else if (lastOperation == LastOperation.COMPANY_CHOSE) {
                        try {
                            int id = Integer.parseInt(messageText);
                            chooseCompanyById(chatId, id);
                        } catch (Exception e) {
                            sendMessage(chatId, "Ошибка при выборе номера компании. Попробуйте снова");
                        }
                    } else if (lastOperation == LastOperation.INPUT_VALUES) {
                        readInputData(chatId, messageText);
                    } else {
                        throw new TelegramApiException();
                    }
                } catch (Exception e) {
                    sendMessage(chatId, "Что-то пошло не так. Попробуйте снова с /start");
                    lastOperation = LastOperation.START;
                }
            } else if (update.hasCallbackQuery()) {
                processClickButton(update);
            }
    }

    public void processClickButton(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        long messageId = update.getCallbackQuery().getMessage().getMessageId();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        try {
            if (callbackData.equals(YES_BUTTON)) {
                String text = "Вы нажали ДА";
                executeEditMessageText(text, chatId, messageId);
                documentHandler.getDocument(inputData[0], companyDaoHandler.getLastCompany().getCounter(), companyDaoHandler.getLastCompany().getRequisites(),
                        Integer.parseInt(inputData[1]), Integer.parseInt(inputData[2]));
                sendDocument(chatId, new File(documentHandler.getOutputFileName()));
                documentHandler.deleteFile();
            } else if (callbackData.equals(NO_BUTTON)) {
                String text = "Вы нажали НЕТ";
                executeEditMessageText(text, chatId, messageId);
            }
        } catch (Exception e){
            sendMessage(chatId, "Что-то пошло не так. Попробуйте снова с /start");
        }
        lastOperation = LastOperation.START;
    }

    private void showAllCompanies(long chatId) {
        try {
            String output = companyDaoHandler.returnAllRecords();
            sendMessage(chatId, output);
            sendMessage(chatId, "Выберите компанию по ее номеру(id)");
            lastOperation = LastOperation.COMPANY_CHOSE;
        } catch (NotFoundException e) {
            sendMessage(chatId, "Нет компаний в базе.\nНачни заново с /start");
        }
    }

    private void chooseCompanyById(long chatId, int id) {
        try {
            Company output = companyDaoHandler.returnCompanyById(id);
            companyDaoHandler.setLastCompany(output);
            sendMessage(chatId, output.toString());
            sendMessage(chatId, "Компания " + "<b>" + output.getName() + "</b>" + " выбрана\n" +
                    "Введите данные о работе в формате:\n" +
                    "<b>" + "Дата, количество часов, цена часа" + "</b>" + "\n" +
                    "<b>" + "Пример: 20.04.2023, 10, 1500" + "</b>");
            lastOperation = LastOperation.INPUT_VALUES;
        } catch (NotFoundException e) {
            sendMessage(chatId, "Нет компаний в базе.\nНачни заново с /start");
        }
    }

    private void readInputData(long chatId, String input) {
        input = input.replace(" ", "");
        if (input.matches("[0-9]{2}[.][0-9]{2}[.][0-9]{4},[0-9]{1,2},[0-9]{4,5}")) {
            inputData = input.split(",");
            sendMessage(chatId, "Введенные данные:");
            StringBuilder check = new StringBuilder();
            check.append("<b>Компания - " + companyDaoHandler.getLastCompany().getName()).append("\n").
                    append("Дата - ").append(inputData[0]).append("\n").
                    append("Часов - ").append(inputData[1]).append("\n").
                    append("Цена - ").append(inputData[2]).append("\n").append("</b>");
            sendMessage(chatId, check.toString());
            sendButtons(chatId);
//            lastOperation = LastOperation.CHECK_CORRECT;
        } else {
            sendMessage(chatId, "Вы ошиблись в вводе данных поездки. Попробуйте снова");
        }
    }

//    private void addCompany(long chatId, String name){
//        String output;
//        try{
//            output = companyDaoHandler.returnAllRecords();
//            is_company_chose = true;
//        }catch (NotFoundException e){
//            output = "Нет компаний в базе.\nНачни заново с /start";
//        }
//        sendMessage(chatId, output);
//    }

    private void startCommandReceived(long chatId, String firstName) throws TelegramApiException {
        String answer = "Здравствуйте, " + firstName + ". Что вы хотите сделать?\n" + "<b>1-Выбрать компанию из имеющихся</b>\n" + "<b>2-Добавить компанию</b>";
        sendMessage(chatId, answer);
        lastOperation = LastOperation.CHOOSING_COMPANIES;
    }

    private void sendDocument(Long chatId, File save) throws TelegramApiException {
        SendDocument sendDocumentRequest = new SendDocument();
        sendDocumentRequest.setChatId(chatId + "");
        sendDocumentRequest.setDocument(new InputFile(save));
        execute(sendDocumentRequest);
    }

    private void sendMessage(long chatId, String message) {
        SendMessage sendMessage = initMessage(chatId, message);
        executeMessage(sendMessage);
    }

    private void sendButtons(long chatId) {
        SendMessage message = initMessage(chatId, "Все ли верно?");

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();

        var yesButton = new InlineKeyboardButton();
        yesButton.setText("Да");
        yesButton.setCallbackData(YES_BUTTON);

        var noButton = new InlineKeyboardButton();
        noButton.setText("Нет");
        noButton.setCallbackData(NO_BUTTON);

        rowInLine.add(yesButton);
        rowInLine.add(noButton);

        rowsInLine.add(rowInLine);

        markupInLine.setKeyboard(rowsInLine);
        message.setReplyMarkup(markupInLine);

        executeMessage(message);
    }


    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private SendMessage initMessage(long chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(text);
        sendMessage.enableHtml(true);
        return sendMessage;
    }

    private void executeEditMessageText(String text, long chatId, long messageId) throws TelegramApiException {
        EditMessageText message = new EditMessageText();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setMessageId((int) messageId);

        execute(message);
    }

}
