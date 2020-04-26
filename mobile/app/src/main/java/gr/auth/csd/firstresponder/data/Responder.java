package gr.auth.csd.firstresponder.data;

import java.util.HashMap;

public class Responder {

    public enum skill {
        STOP_HEAVY_BLEEDING, TREATING_SHOCK, CPR, AED
    }

    private String name, surname, phoneNumber; //Note that phoneNumber acts as id.
    private int acceptedEmergencies, missedEmergiencies;
    private HashMap<skill, Boolean> knownSkills;

    public Responder(String name, String surname){
        {
            this.name = name;
            this.surname = surname;
            this.phoneNumber = "";
            acceptedEmergencies = 0;
            missedEmergiencies = 0;

            // No required skills when creating an account.
            knownSkills = new HashMap<>();
            for (skill x : skill.values()){
                knownSkills.put(x , false);
            }
        }
    }

    public Responder(String name, String surname, String phoneNumber){
        this(name, surname);
        this.phoneNumber = phoneNumber;
    }

    public Responder(String name, String surname, int acceptedEmergencies, int missedEmergiencies){
        this(name, surname);
        this.acceptedEmergencies = acceptedEmergencies;
        this.missedEmergiencies = missedEmergiencies;
    }

    // Getters
    public int getAcceptedEmergencies() { return acceptedEmergencies; }
    public int getMissedEmergiencies() { return missedEmergiencies; }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public String getPhoneNumber() { return phoneNumber; }
    public Boolean canStopHeavyBleeding() { return  knownSkills.get(skill.STOP_HEAVY_BLEEDING); }
    public Boolean canTreatShock() { return knownSkills.get(skill.TREATING_SHOCK); }
    public Boolean canPerformCPR() {return  knownSkills.get(skill.CPR); }
    public Boolean canUseAED() { return knownSkills.get(skill.AED); }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setSurname(String surname) { this.surname = surname; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setKnownSkill(skill key, boolean value) { this.knownSkills.put(key, value); }

    public void acceptedEmergency() { acceptedEmergencies++; }
    public void missedEmergency() { missedEmergiencies++; }
    public void changeKnownSkillStatus(skill key){ knownSkills.put(key, !(knownSkills.get(key))) ;}
}
