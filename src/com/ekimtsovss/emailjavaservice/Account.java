package com.ekimtsovss.emailjavaservice;

import java.io.*;

public final class Account {
    private final String name;
    private final String password;
    public Account(){
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader("C:\\email.txt"))){
            name = bufferedReader.readLine();
            password = bufferedReader.readLine();
            } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    public String getName() {
        return name;
    }
    public String getPassword() {
        return password;
    }
}
