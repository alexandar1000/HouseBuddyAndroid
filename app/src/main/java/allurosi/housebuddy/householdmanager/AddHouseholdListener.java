package allurosi.housebuddy.householdmanager;

public interface AddHouseholdListener {

    void onAddingStart();

    void onAddingFinish(String householdPath);

    void onAddingFailure(int stringResource);

}
