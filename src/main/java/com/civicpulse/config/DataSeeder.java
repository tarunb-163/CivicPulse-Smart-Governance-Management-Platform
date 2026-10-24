package com.civicpulse.config;

import com.civicpulse.entity.*;
import com.civicpulse.enums.ComplaintStatus;
import com.civicpulse.enums.Role;
import com.civicpulse.enums.Urgency;
import com.civicpulse.repository.*;
import com.civicpulse.util.CodeGenerator;
import com.civicpulse.util.ContentNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final CategoryRepository categoryRepository;
    private final KeywordRuleRepository keywordRuleRepository;
    private final ComplaintRepository complaintRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final CommentRepository commentRepository;
    private final FeedbackRepository feedbackRepository;
    private final AnnouncementRepository announcementRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (departmentRepository.count() > 0 && userRepository.count() > 0) {
            String defaultHash = passwordEncoder.encode("Password@123");
            userRepository.findAll().forEach(u -> {
                u.setPasswordHash(defaultHash);
                userRepository.save(u);
            });
            log.info("Database already seeded. Ensured all user account passwords are Password@123. Skipping full reseed.");
            return;
        }

        log.info("Starting comprehensive CivicPulse data seeding...");

        seedSettings();
        Map<String, Department> depts = seedDepartments();
        Map<String, Category> categories = seedCategories(depts);
        seedKeywordRules(categories);
        Map<String, User> users = seedUsers(depts);
        seedAnnouncements();
        seedComplaints(users, categories);

        log.info("CivicPulse data seeding completed successfully! Total complaints: {}", complaintRepository.count());
    }

    private void seedSettings() {
        Map<String, String> defaultSettings = Map.of(
                "duplicate.distance_meters", "300",
                "duplicate.similarity_threshold", "0.65",
                "duplicate.time_window_hours", "24",
                "rate_limit.complaints_per_hour", "5",
                "rate_limit.image_hash_threshold", "10",
                "app.city", "Davanagere",
                "app.state", "Karnataka",
                "app.public-base-url", "http://localhost:8080"
        );
        defaultSettings.forEach((k, v) -> {
            if (systemSettingRepository.findById(k).isEmpty()) {
                systemSettingRepository.save(SystemSetting.builder().settingKey(k).settingValue(v).description("System config").build());
            }
        });
    }

    private Map<String, Department> seedDepartments() {
        Map<String, Department> depts = new LinkedHashMap<>();
        List<Department> list = List.of(
                Department.builder().name("Roads & Infrastructure").code("ROADS").description("Road maintenance, potholes, footpaths and bridges").active(true).build(),
                Department.builder().name("Solid Waste Management").code("SWM").description("Garbage collection, waste disposal and dump clearance").active(true).build(),
                Department.builder().name("Water Supply & Sewerage").code("WATER").description("Drinking water distribution, pipeline repairs and drainage").active(true).build(),
                Department.builder().name("Electricity & Street Lighting").code("ELEC").description("Street lights, electric poles, transformers and high-tension safety").active(true).build(),
                Department.builder().name("Town Planning & Encroachment").code("PLAN").description("Unauthorized construction, illegal hoarding and footpath encroachments").active(true).build(),
                Department.builder().name("Public Health & Sanitation").code("HEALTH").description("Vector control, mosquito breeding, food safety and disease control").active(true).build(),
                Department.builder().name("Parks & Horticulture").code("PARK").description("Public parks maintenance, dangerous tree trimming and gardens").active(true).build(),
                Department.builder().name("Revenue & Property Tax").code("REV").description("Property tax records, khata issues and municipal assessments").active(true).build()
        );
        for (Department d : list) {
            depts.put(d.getCode(), departmentRepository.save(d));
        }
        return depts;
    }

    private Map<String, Category> seedCategories(Map<String, Department> depts) {
        Map<String, Category> cats = new LinkedHashMap<>();
        List<Category> list = List.of(
                // Roads
                Category.builder().name("Pothole & Road Damage").department(depts.get("ROADS")).severityWeight(3).defaultSlaHours(48).active(true).build(),
                Category.builder().name("Broken Footpath / Pedestrian Path").department(depts.get("ROADS")).severityWeight(2).defaultSlaHours(72).active(true).build(),
                Category.builder().name("Missing Manhole Cover").department(depts.get("ROADS")).severityWeight(5).defaultSlaHours(12).active(true).build(),
                Category.builder().name("Illegal Road Cutting").department(depts.get("ROADS")).severityWeight(3).defaultSlaHours(48).active(true).build(),

                // SWM
                Category.builder().name("Garbage Dump / Overflowing Bin").department(depts.get("SWM")).severityWeight(3).defaultSlaHours(24).active(true).build(),
                Category.builder().name("Uncollected Door-to-Door Waste").department(depts.get("SWM")).severityWeight(2).defaultSlaHours(48).active(true).build(),
                Category.builder().name("Dead Animal Disposal").department(depts.get("SWM")).severityWeight(4).defaultSlaHours(12).active(true).build(),
                Category.builder().name("Debris & Construction Waste").department(depts.get("SWM")).severityWeight(2).defaultSlaHours(72).active(true).build(),

                // Water
                Category.builder().name("Water Pipeline Leakage / Burst").department(depts.get("WATER")).severityWeight(4).defaultSlaHours(24).active(true).build(),
                Category.builder().name("Contaminated / Dirty Water Supply").department(depts.get("WATER")).severityWeight(5).defaultSlaHours(12).active(true).build(),
                Category.builder().name("No Water Supply in Area").department(depts.get("WATER")).severityWeight(4).defaultSlaHours(24).active(true).build(),
                Category.builder().name("Drainage / Sewage Overflow").department(depts.get("WATER")).severityWeight(4).defaultSlaHours(18).active(true).build(),

                // Elec
                Category.builder().name("Street Light Not Working").department(depts.get("ELEC")).severityWeight(2).defaultSlaHours(48).active(true).build(),
                Category.builder().name("Hanging Live Electric Wire").department(depts.get("ELEC")).severityWeight(5).defaultSlaHours(6).active(true).build(),
                Category.builder().name("Transformer Sparking / Noise").department(depts.get("ELEC")).severityWeight(5).defaultSlaHours(12).active(true).build(),

                // Plan
                Category.builder().name("Illegal Construction / Encroachment").department(depts.get("PLAN")).severityWeight(3).defaultSlaHours(120).active(true).build(),
                Category.builder().name("Footpath Vendor Encroachment").department(depts.get("PLAN")).severityWeight(2).defaultSlaHours(72).active(true).build(),

                // Health
                Category.builder().name("Mosquito Breeding / Stagnant Water").department(depts.get("HEALTH")).severityWeight(3).defaultSlaHours(36).active(true).build(),
                Category.builder().name("Food Hygiene / Rotten Food at Stall").department(depts.get("HEALTH")).severityWeight(3).defaultSlaHours(48).active(true).build(),
                Category.builder().name("Public Urination / Open Defecation").department(depts.get("HEALTH")).severityWeight(2).defaultSlaHours(48).active(true).build(),

                // Park
                Category.builder().name("Fallen Tree / Dangerous Branch").department(depts.get("PARK")).severityWeight(4).defaultSlaHours(18).active(true).build(),
                Category.builder().name("Damaged Park Equipment / Bench").department(depts.get("PARK")).severityWeight(1).defaultSlaHours(96).active(true).build(),

                // Revenue
                Category.builder().name("Property Tax Assessment Dispute").department(depts.get("REV")).severityWeight(1).defaultSlaHours(168).active(true).build(),

                // Fallback
                Category.builder().name("General / Other Civic Issue").department(depts.get("ROADS")).severityWeight(2).defaultSlaHours(72).active(true).build()
        );

        for (Category c : list) {
            cats.put(c.getName(), categoryRepository.save(c));
        }
        return cats;
    }

    private void seedKeywordRules(Map<String, Category> cats) {
        List<KeywordRule> rules = new ArrayList<>();

        addRules(rules, cats.get("Pothole & Road Damage"), 10,
                "pothole", "potholes", "gudda", "pit", "crater", "cracked road", "broken road", "asphalt damaged", "tar road damaged", "road patch");
        addRules(rules, cats.get("Broken Footpath / Pedestrian Path"), 10,
                "footpath", "broken pavement", "pedestrian", "sidewalk", "paver block", "broken curb", "walkway damaged");
        addRules(rules, cats.get("Missing Manhole Cover"), 15,
                "manhole", "open drain", "chamber cover", "missing manhole", "gutter hole", "open gutter", "open manhole", "sewer cover missing");
        addRules(rules, cats.get("Illegal Road Cutting"), 8,
                "road cutting", "unauthorized trench", "pipe digging road", "cable trench", "digging without permission");

        addRules(rules, cats.get("Garbage Dump / Overflowing Bin"), 10,
                "garbage", "kachra", "dump", "bin overflow", "trash pile", "dustbin overflowing", "littering", "waste heap", "plastic dump", "rotting garbage");
        addRules(rules, cats.get("Uncollected Door-to-Door Waste"), 8,
                "door to door waste", "garbage truck missed", "waste collector absent", "d2d garbage", "kachra gadi");
        addRules(rules, cats.get("Dead Animal Disposal"), 15,
                "dead dog", "dead cow", "carcass", "dead animal", "animal rotting", "foul smell animal");
        addRules(rules, cats.get("Debris & Construction Waste"), 8,
                "debris", "construction waste", "rubble", "c&d waste", "malba", "dumping gravel", "sand pile on road");

        addRules(rules, cats.get("Water Pipeline Leakage / Burst"), 12,
                "water leakage", "pipeline burst", "pipe leaking", "potable water waste", "water gushing", "main pipeline cracked");
        addRules(rules, cats.get("Contaminated / Dirty Water Supply"), 15,
                "dirty water", "muddy water", "black water tap", "contaminated drinking water", "bad smell tap water", "turbid water");
        addRules(rules, cats.get("No Water Supply in Area"), 12,
                "no water", "water stopped", "no water supply", "dry tap", "tanker needed", "low water pressure");
        addRules(rules, cats.get("Drainage / Sewage Overflow"), 12,
                "drainage overflow", "sewage backup", "gutter overflow", "sewer line blocked", "stinking sewage water", "drain choked");

        addRules(rules, cats.get("Street Light Not Working"), 10,
                "street light", "streetlamp", "dark road", "light not glowing", "pole light off", "blinking street light", "bulb fused");
        addRules(rules, cats.get("Hanging Live Electric Wire"), 20,
                "hanging wire", "live wire", "open wire", "snapped cable", "electric shock risk", "wire dangling");
        addRules(rules, cats.get("Transformer Sparking / Noise"), 15,
                "transformer spark", "blast transformer", "sparking pole", "transformer buzzing", "smoke from transformer");

        addRules(rules, cats.get("Illegal Construction / Encroachment"), 10,
                "illegal construction", "encroachment", "building without permit", "setback violation", "occupying government land");
        addRules(rules, cats.get("Footpath Vendor Encroachment"), 8,
                "vendor blocking", "illegal hawker", "stall on footpath", "shop extending on road", "shed blocking walk");

        addRules(rules, cats.get("Mosquito Breeding / Stagnant Water"), 10,
                "mosquito", "dengue", "larvae", "stagnant water pool", "spraying needed", "fogging needed");
        addRules(rules, cats.get("Food Hygiene / Rotten Food at Stall"), 10,
                "food hygiene", "rotten food", "spoiled food", "hotel dirty kitchen", "street food adulterated");
        addRules(rules, cats.get("Public Urination / Open Defecation"), 8,
                "public urination", "open defecation", "toilet dirty", "public toilet locked");

        addRules(rules, cats.get("Fallen Tree / Dangerous Branch"), 12,
                "fallen tree", "branch broken", "tree blocking road", "danger tree fall", "tree touching wire");
        addRules(rules, cats.get("Damaged Park Equipment / Bench"), 6,
                "park bench", "broken swing", "park light", "play area damaged", "jogging track broken");

        addRules(rules, cats.get("Property Tax Assessment Dispute"), 8,
                "property tax", "khata", "tax assessment wrong", "double tax notice", "pid number issue");

        keywordRuleRepository.saveAll(rules);
    }

    private void addRules(List<KeywordRule> list, Category category, int weight, String... keywords) {
        if (category == null) return;
        for (String kw : keywords) {
            list.add(KeywordRule.builder().category(category).keyword(kw.toLowerCase().trim()).weight(weight).build());
        }
    }

    private Map<String, User> seedUsers(Map<String, Department> depts) {
        Map<String, User> users = new HashMap<>();

        // Admin
        User admin = User.builder()
                .name("Rajesh Sharma")
                .email("admin@civicpulse.test")
                .passwordHash(passwordEncoder.encode("Password@123"))
                .phone("+91 9876543210")
                .role(Role.ADMIN)
                .enabled(true)
                .emailVerified(true)
                .consentVersion("v1.0")
                .consentAt(Instant.now().minus(100, ChronoUnit.DAYS))
                .inAppNotifications(true)
                .emailNotifications(true)
                .build();
        users.put("admin", userRepository.save(admin));

        // Dept Heads
        Map<String, String[]> deptHeads = Map.of(
                "ROADS", new String[]{"Suresh Patil", "head.roads@civicpulse.test", "+91 9876543211"},
                "SWM", new String[]{"Pooja Hegde", "head.swm@civicpulse.test", "+91 9876543212"},
                "WATER", new String[]{"Anand Kulkarni", "head.water@civicpulse.test", "+91 9876543213"},
                "ELEC", new String[]{"Deepa Nayak", "head.elec@civicpulse.test", "+91 9876543214"}
        );

        deptHeads.forEach((deptCode, info) -> {
            User head = User.builder()
                    .name(info[0])
                    .email(info[1])
                    .passwordHash(passwordEncoder.encode("Password@123"))
                    .phone(info[2])
                    .role(Role.DEPT_HEAD)
                    .department(depts.get(deptCode))
                    .enabled(true)
                    .emailVerified(true)
                    .consentVersion("v1.0")
                    .consentAt(Instant.now().minus(90, ChronoUnit.DAYS))
                    .inAppNotifications(true)
                    .emailNotifications(true)
                    .build();
            users.put("head." + deptCode.toLowerCase(), userRepository.save(head));
        });

        // Officers (at least 2 per main dept, 1 for other depts)
        List<Object[]> officerList = List.of(
                new Object[]{"Manoj Kumar", "officer.roads@civicpulse.test", "+91 9876543221", "ROADS"},
                new Object[]{"Kavitha Reddy", "officer.roads2@civicpulse.test", "+91 9876543222", "ROADS"},
                new Object[]{"Girish Gouda", "officer.swm@civicpulse.test", "+91 9876543223", "SWM"},
                new Object[]{"Asha Shastry", "officer.swm2@civicpulse.test", "+91 9876543224", "SWM"},
                new Object[]{"Raghavendra Rao", "officer.water@civicpulse.test", "+91 9876543225", "WATER"},
                new Object[]{"Sunita Kamat", "officer.water2@civicpulse.test", "+91 9876543226", "WATER"},
                new Object[]{"Pradeep Joshi", "officer.elec@civicpulse.test", "+91 9876543227", "ELEC"},
                new Object[]{"Vijayalakshmi B", "officer.elec2@civicpulse.test", "+91 9876543228", "ELEC"},
                new Object[]{"Chetan Hiremath", "officer.plan@civicpulse.test", "+91 9876543229", "PLAN"},
                new Object[]{"Dr. Santosh Banakar", "officer.health@civicpulse.test", "+91 9876543230", "HEALTH"},
                new Object[]{"Naveen Siddi", "officer.park@civicpulse.test", "+91 9876543231", "PARK"},
                new Object[]{"Lakshmi Kantha", "officer.rev@civicpulse.test", "+91 9876543232", "REV"}
        );

        for (Object[] o : officerList) {
            User officer = User.builder()
                    .name((String) o[0])
                    .email((String) o[1])
                    .passwordHash(passwordEncoder.encode("Password@123"))
                    .phone((String) o[2])
                    .role(Role.OFFICER)
                    .department(depts.get(o[3]))
                    .enabled(true)
                    .emailVerified(true)
                    .consentVersion("v1.0")
                    .consentAt(Instant.now().minus(80, ChronoUnit.DAYS))
                    .inAppNotifications(true)
                    .emailNotifications(true)
                    .build();
            users.put((String) o[1], userRepository.save(officer));
        }

        // Citizens (3 citizens)
        List<String[]> citizens = List.of(
                new String[]{"Rohan Deshmukh", "citizen@civicpulse.test", "+91 9876500001"},
                new String[]{"Priya Varma", "citizen2@civicpulse.test", "+91 9876500002"},
                new String[]{"Kiran Kumar", "citizen3@civicpulse.test", "+91 9876500003"}
        );

        for (String[] c : citizens) {
            User citizen = User.builder()
                    .name(c[0])
                    .email(c[1])
                    .passwordHash(passwordEncoder.encode("Password@123"))
                    .phone(c[2])
                    .role(Role.CITIZEN)
                    .enabled(true)
                    .emailVerified(true)
                    .consentVersion("v1.0")
                    .consentAt(Instant.now().minus(60, ChronoUnit.DAYS))
                    .inAppNotifications(true)
                    .emailNotifications(true)
                    .build();
            users.put(c[1], userRepository.save(citizen));
        }

        return users;
    }

    private void seedAnnouncements() {
        announcementRepository.save(Announcement.builder()
                .title("Monsoon Preparedness Drive in Davanagere")
                .body("Special 24x7 municipal emergency control room activated for low-lying waterlogging and tree fall complaints across all 45 wards.")
                .active(true)
                .startsAt(Instant.now().minus(5, ChronoUnit.DAYS))
                .endsAt(Instant.now().plus(25, ChronoUnit.DAYS))
                .build());

        announcementRepository.save(Announcement.builder()
                .title("Plastic Waste Segregation Campaign")
                .body("Citizen awareness campaign for door-to-door dry and wet waste segregation. Blue and green bin distribution starts this week.")
                .active(true)
                .startsAt(Instant.now().minus(2, ChronoUnit.DAYS))
                .endsAt(Instant.now().plus(15, ChronoUnit.DAYS))
                .build());
    }

    private void seedComplaints(Map<String, User> users, Map<String, Category> categories) {
        List<User> citizenList = List.of(
                users.get("citizen@civicpulse.test"),
                users.get("citizen2@civicpulse.test"),
                users.get("citizen3@civicpulse.test")
        );

        List<User> officerList = new ArrayList<>(users.values().stream()
                .filter(u -> u.getRole() == Role.OFFICER)
                .toList());

        List<Category> catList = new ArrayList<>(categories.values());

        // Davanagere localities with approximate lat/lng offsets
        String[] localities = {
                "MCC 'A' Block, Near Bapuji Hospital",
                "MCC 'B' Block, 5th Main Road",
                "Vidyanagar, 2nd Cross, Near Stadium",
                "PB Road, Near Old Bus Stand",
                "Hadadi Road, Near Reliance Fresh",
                "Jayadeva Circle, Main Market Road",
                "Shamnur Village Road, Near Lake",
                "Anjaneya Badavane, 3rd Stage",
                "Nittuvalli, Near Durgambika Temple",
                "DCM Township, Behind Mill",
                "Saraswathi Nagar, 1st Cross",
                "KTJ Nagar, Main Bazaar"
        };

        double centerLat = 14.4644;
        double centerLng = 75.9218;

        Random random = new Random(42); // deterministic seed for reproducibility

        // Realistic template titles and descriptions per category
        Map<String, String[][]> complaintTemplates = Map.of(
                "Pothole & Road Damage", new String[][]{
                        {"Massive dangerous pothole on main road", "Deep pothole causing bike skids and traffic slowdown near junction during peak hours."},
                        {"Multiple craters after heavy monsoon rain", "Tar has completely eroded leaving large exposed stones and sharp gravel on the turning."},
                        {"Uneven road surface and sinking asphalt", "The road was freshly paved 3 months ago but has already sunken creating a ditch."}
                },
                "Garbage Dump / Overflowing Bin", new String[][]{
                        {"Overflowing garbage bin attracting stray cattle", "Dustbin not cleared for 4 days. Stench is unbearable and dogs are scattering waste across the road."},
                        {"Illegal waste dumping on vacant plot", "Nearby commercial shops are dumping thermocol and food waste creating an open breeding ground."},
                        {"Unattended garbage heap near school entrance", "Piles of plastic and rotten vegetable waste right next to the school gate."}
                },
                "Water Pipeline Leakage / Burst", new String[][]{
                        {"Drinking water pipeline burst wasting thousands of liters", "Water is gushing out like a fountain from the underground pipe flooding the footpath."},
                        {"Continuous leak at road junction valve", "Clean drinking water has been running into the open drain for over 24 hours."},
                        {"Underground pipe crack flooding residential basements", "Water pressure in the street has dropped to zero while water is seeping into cellars."}
                },
                "Street Light Not Working", new String[][]{
                        {"Entire lane pitch dark due to faulty streetlamps", "Three consecutive sodium vapor lights have been off for a week making women feel unsafe at night."},
                        {"Street light pole flickering and sparking", "Light keeps turning on and off with buzzing sounds. Potential short circuit hazard."},
                        {"Damaged lamp post hit by delivery truck", "Pole is leaning precariously at a 45 degree angle toward the pavement."}
                },
                "Missing Manhole Cover", new String[][]{
                        {"Open manhole cover on busy road crossing", "Chamber cover has collapsed into the sewer. High risk of severe accidents for two-wheelers."},
                        {"Broken concrete drain slab on footpath", "Pedestrians can fall into the 6-foot deep drain at night. Temporary tree branch kept as warning."}
                },
                "Drainage / Sewage Overflow", new String[][]{
                        {"Sewage water overflowing into residential street", "Manhole is overflowing with black foul-smelling sewage spreading across front gates."},
                        {"Choked storm water drain causing street waterlogging", "Silt and plastic waste have blocked the culvert so even mild rain floods the road."}
                }
        );

        List<Complaint> seededComplaints = new ArrayList<>();

        // Generate ~200 complaints spread across 90 days
        for (int i = 0; i < 200; i++) {
            Category category = catList.get(random.nextInt(catList.size()));
            Department dept = category.getDepartment();

            // Find matching officer in department
            List<User> deptOfficers = officerList.stream()
                    .filter(u -> u.getDepartment() != null && u.getDepartment().getId().equals(dept.getId()))
                    .toList();
            User assignedOfficer = deptOfficers.isEmpty() ? null : deptOfficers.get(random.nextInt(deptOfficers.size()));

            User citizen = citizenList.get(random.nextInt(citizenList.size()));
            String locality = localities[random.nextInt(localities.length)];

            // Random coordinates around Davanagere center (~3 km radius)
            double lat = centerLat + (random.nextDouble() - 0.5) * 0.045;
            double lng = centerLng + (random.nextDouble() - 0.5) * 0.045;

            // Pick title and description
            String[][] templates = complaintTemplates.get(category.getName());
            String title;
            String desc;
            if (templates != null) {
                String[] t = templates[random.nextInt(templates.length)];
                title = t[0];
                desc = t[1] + " Location: " + locality;
            } else {
                title = category.getName() + " issue reported";
                desc = "Urgent civic attention requested for " + category.getName().toLowerCase() + " near " + locality;
            }

            // Distribute created date between 1 and 90 days ago
            int daysAgo = random.nextInt(90);
            Instant createdAt = Instant.now().minus(daysAgo, ChronoUnit.DAYS).minus(random.nextInt(24), ChronoUnit.HOURS).minus(random.nextInt(60), ChronoUnit.MINUTES);

            // Status distribution: older complaints more likely to be RESOLVED/CLOSED
            ComplaintStatus status;
            if (daysAgo > 30) {
                status = random.nextDouble() < 0.75 ?
                        (random.nextDouble() < 0.5 ? ComplaintStatus.RESOLVED : ComplaintStatus.CLOSED) :
                        (random.nextDouble() < 0.5 ? ComplaintStatus.IN_PROGRESS : ComplaintStatus.ESCALATED);
            } else if (daysAgo > 10) {
                status = random.nextDouble() < 0.4 ? ComplaintStatus.IN_PROGRESS :
                        (random.nextDouble() < 0.3 ? ComplaintStatus.RESOLVED :
                                (random.nextDouble() < 0.5 ? ComplaintStatus.ASSIGNED : ComplaintStatus.ESCALATED));
            } else {
                status = random.nextDouble() < 0.35 ? ComplaintStatus.SUBMITTED :
                        (random.nextDouble() < 0.35 ? ComplaintStatus.ASSIGNED : ComplaintStatus.IN_PROGRESS);
            }

            Urgency urgency = Urgency.values()[random.nextInt(Urgency.values().length)];
            int severity = category.getSeverityWeight();
            int upvotes = random.nextInt(25);
            double priorityScore = (urgency.getWeight() * 3.0) + (severity * 2.0) + Math.min(upvotes, 20) * 0.5 + Math.min(daysAgo, 30) * 0.7;

            Instant slaDueAt = createdAt.plus(category.getDefaultSlaHours(), ChronoUnit.HOURS);
            boolean isOverdue = Instant.now().isAfter(slaDueAt) && (status == ComplaintStatus.ASSIGNED || status == ComplaintStatus.IN_PROGRESS || status == ComplaintStatus.ESCALATED);
            int escalatedLevel = isOverdue ? (random.nextDouble() < 0.5 ? 1 : 2) : 0;
            if (escalatedLevel > 0 && status != ComplaintStatus.RESOLVED && status != ComplaintStatus.CLOSED) {
                status = ComplaintStatus.ESCALATED;
            }

            Instant resolvedAt = (status == ComplaintStatus.RESOLVED || status == ComplaintStatus.CLOSED) ?
                    createdAt.plus(Math.max(4, random.nextInt(Math.max(5, category.getDefaultSlaHours()))), ChronoUnit.HOURS) : null;

            String code = CodeGenerator.generate(2026, (long) (i + 1));
            String contentHash = ContentNormalizer.hash(title + " " + desc);

            Complaint complaint = Complaint.builder()
                    .publicCode(code)
                    .title(title)
                    .description(desc)
                    .contentHash(contentHash)
                    .category(category)
                    .department(dept)
                    .citizen(citizen)
                    .assignedOfficer(assignedOfficer)
                    .status(status)
                    .urgency(urgency)
                    .latitude(lat)
                    .longitude(lng)
                    .address(locality + ", Davanagere, Karnataka")
                    .anonymous(random.nextDouble() < 0.1)
                    .priorityScore(priorityScore)
                    .slaDueAt(slaDueAt)
                    .escalationLevel(escalatedLevel)
                    .upvoteCount(upvotes)
                    .resolvedAt(resolvedAt)
                    .createdAt(createdAt)
                    .updatedAt(resolvedAt != null ? resolvedAt : createdAt)
                    .build();

            seededComplaints.add(complaint);
        }

        List<Complaint> savedComplaints = complaintRepository.saveAll(seededComplaints);

        // Seed StatusHistories, Feedback, and Comments
        List<StatusHistory> histories = new ArrayList<>();
        List<Feedback> feedbacks = new ArrayList<>();
        List<Comment> comments = new ArrayList<>();

        for (Complaint c : savedComplaints) {
            // Initial SUBMITTED history
            histories.add(StatusHistory.builder()
                    .complaint(c)
                    .fromStatus(null)
                    .toStatus(ComplaintStatus.SUBMITTED)
                    .changedBy(c.getCitizen())
                    .changedByName(c.getCitizen().getName())
                    .remark("Complaint registered by citizen")
                    .createdAt(c.getCreatedAt())
                    .build());

            if (c.getStatus() != ComplaintStatus.SUBMITTED) {
                histories.add(StatusHistory.builder()
                        .complaint(c)
                        .fromStatus(ComplaintStatus.SUBMITTED)
                        .toStatus(ComplaintStatus.ASSIGNED)
                        .changedBy(c.getAssignedOfficer() != null ? c.getAssignedOfficer() : users.get("admin"))
                        .changedByName(c.getAssignedOfficer() != null ? c.getAssignedOfficer().getName() : "System Auto-Router")
                        .remark("Auto-routed and assigned to department officer")
                        .createdAt(c.getCreatedAt().plus(15, ChronoUnit.MINUTES))
                        .build());
            }

            if (c.getStatus() == ComplaintStatus.IN_PROGRESS || c.getStatus() == ComplaintStatus.RESOLVED || c.getStatus() == ComplaintStatus.CLOSED || c.getStatus() == ComplaintStatus.ESCALATED) {
                histories.add(StatusHistory.builder()
                        .complaint(c)
                        .fromStatus(ComplaintStatus.ASSIGNED)
                        .toStatus(ComplaintStatus.IN_PROGRESS)
                        .changedBy(c.getAssignedOfficer() != null ? c.getAssignedOfficer() : users.get("admin"))
                        .changedByName(c.getAssignedOfficer() != null ? c.getAssignedOfficer().getName() : "Officer")
                        .remark("Inspection team dispatched to site for verification")
                        .createdAt(c.getCreatedAt().plus(2, ChronoUnit.HOURS))
                        .build());
            }

            if (c.getStatus() == ComplaintStatus.RESOLVED || c.getStatus() == ComplaintStatus.CLOSED) {
                histories.add(StatusHistory.builder()
                        .complaint(c)
                        .fromStatus(ComplaintStatus.IN_PROGRESS)
                        .toStatus(ComplaintStatus.RESOLVED)
                        .changedBy(c.getAssignedOfficer() != null ? c.getAssignedOfficer() : users.get("admin"))
                        .changedByName(c.getAssignedOfficer() != null ? c.getAssignedOfficer().getName() : "Officer")
                        .remark("Field work completed and verified with photographic evidence")
                        .createdAt(c.getResolvedAt() != null ? c.getResolvedAt() : c.getCreatedAt().plus(24, ChronoUnit.HOURS))
                        .build());

                // Seed feedback for ~70% of resolved complaints
                if (random.nextDouble() < 0.70) {
                    int rating = 3 + random.nextInt(3); // 3, 4, or 5 stars
                    feedbacks.add(Feedback.builder()
                            .complaint(c)
                            .rating(rating)
                            .satisfied(rating >= 3)
                            .comment(rating >= 4 ? "Thank you for the prompt resolution! Great work by the city team." : "Work was completed after delay, please ensure timely cleanup next time.")
                            .createdAt(c.getResolvedAt() != null ? c.getResolvedAt().plus(2, ChronoUnit.HOURS) : Instant.now())
                            .build());
                }
            }

            if (c.getStatus() == ComplaintStatus.ESCALATED) {
                histories.add(StatusHistory.builder()
                        .complaint(c)
                        .fromStatus(ComplaintStatus.IN_PROGRESS)
                        .toStatus(ComplaintStatus.ESCALATED)
                        .changedBy(users.get("admin"))
                        .changedByName("System SLA Monitor")
                        .remark("SLA deadline exceeded. Automatically escalated to Department Head.")
                        .createdAt(c.getSlaDueAt().plus(15, ChronoUnit.MINUTES))
                        .build());
            }

            // Seed a citizen/officer comment on 25% of complaints
            if (random.nextDouble() < 0.25) {
                comments.add(Comment.builder()
                        .complaint(c)
                        .author(c.getCitizen())
                        .body("Adding landmark note: this is right opposite the government primary school gate.")
                        .internal(false)
                        .createdAt(c.getCreatedAt().plus(1, ChronoUnit.HOURS))
                        .build());
            }
        }

        statusHistoryRepository.saveAll(histories);
        feedbackRepository.saveAll(feedbacks);
        commentRepository.saveAll(comments);
    }
}
