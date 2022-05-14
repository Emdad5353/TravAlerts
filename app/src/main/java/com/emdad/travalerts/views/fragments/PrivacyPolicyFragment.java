package com.emdad.travalerts.views.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.emdad.travalerts.R;
import com.emdad.travalerts.databinding.FragmentPrivacyPolicyBinding;

public class PrivacyPolicyFragment extends Fragment {

    private FragmentPrivacyPolicyBinding binding;

    public PrivacyPolicyFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPrivacyPolicyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.tvPrivacyPolicy.setText(Html.fromHtml("<strong>Privacy Policy</strong>\n" +
                "        <p>\n" +
                "                  Emdadur Rahman built the Travalerts app as\n" +
                "                  a Free app. This SERVICE is provided by\n" +
                "                  Emdadur Rahman at no cost and is intended for use as\n" +
                "                  is.\n" +
                "                </p> <p>\n" +
                "                  This page is used to inform visitors regarding my\n" +
                "                  policies with the collection, use, and disclosure of Personal\n" +
                "                  Information if anyone decided to use my Service.\n" +
                "                </p> <p>\n" +
                "                  If you choose to use my Service, then you agree to\n" +
                "                  the collection and use of information in relation to this\n" +
                "                  policy. The Personal Information that I collect is\n" +
                "                  used for providing and improving the Service. I will not use or share your information with\n" +
                "                  anyone except as described in this Privacy Policy.\n" +
                "                </p> <p>\n" +
                "                  The terms used in this Privacy Policy have the same meanings\n" +
                "                  as in our Terms and Conditions, which are accessible at\n" +
                "                  Travalerts unless otherwise defined in this Privacy Policy.\n" +
                "                </p> <p><strong>Information Collection and Use</strong></p> <p>\n" +
                "                  For a better experience, while using our Service, I\n" +
                "                  may require you to provide us with certain personally\n" +
                "                  identifiable information. The information that\n" +
                "                  I request will be retained on your device and is not collected by me in any way.\n" +
                "                </p> <div><p>\n" +
                "                    The app does use third-party services that may collect\n" +
                "                    information used to identify you.\n" +
                "                  </p> <p>\n" +
                "                    Link to the privacy policy of third-party service providers used\n" +
                "                    by the app\n" +
                "                  </p> <ul><li><a href=\"https://www.google.com/policies/privacy/\" rel=\"noopener noreferrer\" target=\"_blank\">Google Play Services</a></li>\n" +
                "    </ul></div> <p><strong>Log Data</strong></p> <p>\n" +
                "                  I want to inform you that whenever you\n" +
                "                  use my Service, in a case of an error in the app\n" +
                "                  I collect data and information (through third-party\n" +
                "                  products) on your phone called Log Data. This Log Data may\n" +
                "                  include information such as your device Internet Protocol\n" +
                "                  (“IP”) address, device name, operating system version, the\n" +
                "                  configuration of the app when utilizing my Service,\n" +
                "                  the time and date of your use of the Service, and other\n" +
                "                  statistics.\n" +
                "                </p> <p><strong>Cookies</strong></p> <p>\n" +
                "                  Cookies are files with a small amount of data that are\n" +
                "                  commonly used as anonymous unique identifiers. These are sent\n" +
                "                  to your browser from the websites that you visit and are\n" +
                "                  stored on your device\\'s internal memory.\n" +
                "                </p> <p>\n" +
                "                  This Service does not use these “cookies” explicitly. However,\n" +
                "                  the app may use third-party code and libraries that use\n" +
                "                  “cookies” to collect information and improve their services.\n" +
                "                  You have the option to either accept or refuse these cookies\n" +
                "                  and know when a cookie is being sent to your device. If you\n" +
                "                  choose to refuse our cookies, you may not be able to use some\n" +
                "                  portions of this Service.\n" +
                "                </p> <p><strong>Service Providers</strong></p> <p>\n" +
                "                  I may employ third-party companies and\n" +
                "                  individuals due to the following reasons:\n" +
                "                </p> <ul><li>To facilitate our Service;</li> <li>To provide the Service on our behalf;</li> <li>To perform Service-related services; or</li> <li>To assist us in analyzing how our Service is used.</li></ul> <p>\n" +
                "                  I want to inform users of this Service\n" +
                "                  that these third parties have access to their Personal\n" +
                "                  Information. The reason is to perform the tasks assigned to\n" +
                "                  them on our behalf. However, they are obligated not to\n" +
                "                  disclose or use the information for any other purpose.\n" +
                "                </p> <p><strong>Security</strong></p> <p>\n" +
                "                  I value your trust in providing us your\n" +
                "                  Personal Information, thus we are striving to use commercially\n" +
                "                  acceptable means of protecting it. But remember that no method\n" +
                "                  of transmission over the internet, or method of electronic\n" +
                "                  storage is 100% secure and reliable, and I cannot\n" +
                "                  guarantee its absolute security.\n" +
                "                </p> <p><strong>Links to Other Sites</strong></p> <p>\n" +
                "                  This Service may contain links to other sites. If you click on\n" +
                "                  a third-party link, you will be directed to that site. Note\n" +
                "                  that these external sites are not operated by me.\n" +
                "                  Therefore, I strongly advise you to review the\n" +
                "                  Privacy Policy of these websites. I have\n" +
                "                  no control over and assume no responsibility for the content,\n" +
                "                  privacy policies, or practices of any third-party sites or\n" +
                "                  services.\n" +
                "                </p> <p><strong>Children’s Privacy</strong></p> <div><p>\n" +
                "                    These Services do not address anyone under the age of 13.\n" +
                "                    I do not knowingly collect personally\n" +
                "                    identifiable information from children under 13 years of age. In the case\n" +
                "                    I discover that a child under 13 has provided\n" +
                "                    me with personal information, I immediately\n" +
                "                    delete this from our servers. If you are a parent or guardian\n" +
                "                    and you are aware that your child has provided us with\n" +
                "                    personal information, please contact me so that\n" +
                "                    I will be able to do the necessary actions.\n" +
                "                  </p></div> <!---->\n" +
                "        <p><strong>Changes to This Privacy Policy</strong></p> <p>\n" +
                "                  I may update our Privacy Policy from\n" +
                "                  time to time. Thus, you are advised to review this page\n" +
                "                  periodically for any changes. I will\n" +
                "                  notify you of any changes by posting the new Privacy Policy on\n" +
                "                  this page.\n" +
                "                </p> <p>This policy is effective as of 2021-12-15</p> <p><strong>Contact Us</strong></p> <p>\n" +
                "                  If you have any questions or suggestions about my\n" +
                "                  Privacy Policy, do not hesitate to contact me at travalerts@gmail.com.\n" +
                "                </p> <p>This privacy policy page was created at <a href=\"https://privacypolicytemplate.net\" rel=\"noopener noreferrer\" target=\"_blank\">privacypolicytemplate.net </a>and modified/generated by <a href=\"https://app-privacy-policy-generator.nisrulz.com/\" rel=\"noopener noreferrer\" target=\"_blank\">App Privacy Policy Generator</a></p>"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}