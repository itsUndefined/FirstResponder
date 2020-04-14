package gr.auth.csd.firstresponder.data;

import java.util.HashMap;

public class Responder {

    enum skill {
        STOP_HEAVY_BLEEDING, TREATING_SHOCK, USE_AED, CPR
    }

    private String name, surname, phoneNumber; //Note that phoneNumber acts as id.
    private int acceptedEmergencies, missedEmergiencies;
    private HashMap<skill, Boolean> knownSkills;

    public Responder(String name, String surname, String phoneNumber){
        this.name = name;
        this.surname = surname;
        this.phoneNumber = phoneNumber;
        acceptedEmergencies = 0;
        missedEmergiencies = 0;

        // No required skills when creating an account.
        knownSkills = new HashMap<>();
        for (skill x : skill.values()){
            knownSkills.put(x , false);
        }
    }

    // Getters
    public int getAcceptedEmergencies() { return acceptedEmergencies; }
    public int getMissedEmergiencies() { return missedEmergiencies; }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public String getPhoneNumber() { return phoneNumber; }
    public HashMap getKnownSkills() { return knownSkills; }

    public void acceptedEmergency() { acceptedEmergencies++; }
    public void missedEmergency() { missedEmergiencies++; }
    public void changeKnownSkillStatus(skill key){ knownSkills.put(key, !(knownSkills.get(key))) ;}
}
