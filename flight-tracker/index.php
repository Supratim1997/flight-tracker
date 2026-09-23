<!DOCTYPE html>
<html lang="en" class="dark">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Flight Scanner | Premium Monitoring</title>
    
    <!-- Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    
    <!-- Tailwind CSS -->
    <script src="https://cdn.tailwindcss.com"></script>
    <script>
        tailwind.config = {
            darkMode: 'class',
            theme: {
                extend: {
                    fontFamily: {
                        sans: ['Outfit', 'sans-serif'],
                    },
                    colors: {
                        primary: '#6366f1',
                        secondary: '#ec4899',
                        darkBg: '#0f172a',
                        cardBg: 'rgba(30, 41, 59, 0.7)',
                    }
                }
            }
        }
    </script>
    
    <!-- Chart.js -->
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    
    <!-- Custom Styles -->
    <style>
        body {
            background-color: #0f172a;
            background-image: 
                radial-gradient(at 0% 0%, hsla(253,16%,7%,1) 0, transparent 50%), 
                radial-gradient(at 50% 0%, hsla(225,39%,30%,1) 0, transparent 50%), 
                radial-gradient(at 100% 0%, hsla(339,49%,30%,1) 0, transparent 50%);
            background-attachment: fixed;
            color: #f8fafc;
        }
        .glass-panel {
            background: rgba(30, 41, 59, 0.4);
            backdrop-filter: blur(12px);
            -webkit-backdrop-filter: blur(12px);
            border: 1px solid rgba(255, 255, 255, 0.08);
            box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
        }
        .input-glass {
            background: rgba(15, 23, 42, 0.6);
            border: 1px solid rgba(255, 255, 255, 0.1);
            color: white;
            transition: all 0.3s ease;
        }
        .input-glass:focus {
            outline: none;
            border-color: #6366f1;
            box-shadow: 0 0 15px rgba(99, 102, 241, 0.3);
        }
        .btn-gradient {
            background: linear-gradient(135deg, #6366f1 0%, #ec4899 100%);
            transition: transform 0.2s, box-shadow 0.2s;
        }
        .btn-gradient:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 25px -5px rgba(236, 72, 153, 0.5);
        }
        /* Custom scrollbar */
        ::-webkit-scrollbar { width: 8px; }
        ::-webkit-scrollbar-track { background: #0f172a; }
        ::-webkit-scrollbar-thumb { background: #334155; border-radius: 4px; }
        ::-webkit-scrollbar-thumb:hover { background: #475569; }
    </style>
</head>
<body class="min-h-screen p-4 md:p-8 flex flex-col items-center">

    <header class="w-full max-w-7xl mb-8 flex justify-between items-center animate-fade-in-down">
        <div>
            <h1 class="text-4xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-primary to-secondary">FlightTracker</h1>
            <p class="text-slate-400 text-sm mt-1">Premium Direct Flight Monitoring</p>
        </div>
        <div class="flex items-center gap-3">
            <button onclick="openSettingsModal()" class="bg-slate-800/80 hover:bg-slate-700 text-slate-200 border border-slate-700 px-5 py-2 rounded-full font-medium flex items-center gap-2 transition-all shadow-md hover:border-indigo-500">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-indigo-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
                <span>Settings</span>
            </button>
            <button onclick="triggerScrape()" id="scrapeBtn" class="btn-gradient text-white px-6 py-2 rounded-full font-medium shadow-lg flex items-center gap-2">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                </svg>
                <span id="scrapeBtnText">Run Manual Check</span>
            </button>
        </div>
    </header>

    <!-- Searchable Autocomplete Datalist for Indian Airports -->
    <datalist id="indianAirports">
        <option value="DEL">DEL - New Delhi (Indira Gandhi Intl)</option>
        <option value="BOM">BOM - Mumbai (Chhatrapati Shivaji Maharaj Intl)</option>
        <option value="BLR">BLR - Bengaluru (Kempegowda Intl)</option>
        <option value="MAA">MAA - Chennai Intl</option>
        <option value="CCU">CCU - Kolkata (Netaji Subhash Chandra Bose Intl)</option>
        <option value="HYD">HYD - Hyderabad (Rajiv Gandhi Intl)</option>
        <option value="PNQ">PNQ - Pune Airport</option>
        <option value="AMD">AMD - Ahmedabad (Sardar Vallabhbhai Patel Intl)</option>
        <option value="GOI">GOI - Goa (Dabolim Airport)</option>
        <option value="GOX">GOX - Goa (Manohar Intl Airport, Mopa)</option>
        <option value="COK">COK - Kochi (Cochin Intl)</option>
        <option value="TRV">TRV - Thiruvananthapuram Intl</option>
        <option value="JAI">JAI - Jaipur Intl</option>
        <option value="LKO">LKO - Lucknow (Chaudhary Charan Singh Intl)</option>
        <option value="ATQ">ATQ - Amritsar (Sri Guru Ram Dass Jee Intl)</option>
        <option value="GAU">GAU - Guwahati (Lokpriya Gopinath Bordoloi Intl)</option>
        <option value="IXB">IXB - Bagdogra (Siliguri)</option>
        <option value="VNS">VNS - Varanasi (Lal Bahadur Shastri Intl)</option>
        <option value="PAT">PAT - Patna (Jay Prakash Narayan)</option>
        <option value="BBI">BBI - Bhubaneswar (Biju Patnaik Intl)</option>
        <option value="IXC">IXC - Chandigarh Intl</option>
        <option value="SXR">SXR - Srinagar (Sheikh ul-Alam Intl)</option>
    </datalist>

    <main class="w-full max-w-7xl grid grid-cols-1 lg:grid-cols-3 gap-8">
        
        <!-- Left Column: Config Panel -->
        <div class="lg:col-span-1 space-y-8">
            <div class="glass-panel rounded-2xl p-6">
                <div class="flex justify-between items-center mb-6">
                    <h2 class="text-xl font-semibold flex items-center gap-2">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-primary" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4" />
                        </svg>
                        <span id="formTitle">Tracking Configuration</span>
                    </h2>
                    <button type="button" onclick="resetConfigForm()" class="text-xs bg-indigo-600/30 hover:bg-indigo-600/60 text-indigo-300 border border-indigo-500/40 px-3 py-1.5 rounded-lg transition-all flex items-center gap-1 shadow-sm">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
                        </svg>
                        + New Route
                    </button>
                </div>
                <form id="configForm" class="space-y-4" onsubmit="saveConfig(event)">
                    <input type="hidden" id="configId" name="id" value="0">
                    
                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <label class="block text-xs text-slate-400 mb-1">Departure (Indian Airport)</label>
                            <input type="text" id="departure" name="departure_city" list="indianAirports" required class="input-glass w-full rounded-lg px-4 py-2 uppercase" placeholder="DEL, Mumbai, etc." onchange="cleanAirportInput(this)" oninput="cleanAirportInput(this)">
                        </div>
                        <div>
                            <label class="block text-xs text-slate-400 mb-1">Arrival (Indian Airport)</label>
                            <input type="text" id="arrival" name="arrival_city" list="indianAirports" required class="input-glass w-full rounded-lg px-4 py-2 uppercase" placeholder="BOM, Pune, etc." onchange="cleanAirportInput(this)" oninput="cleanAirportInput(this)">
                        </div>
                    </div>
                    
                    <div>
                        <label class="block text-xs text-slate-400 mb-1">Preferred Date</label>
                        <input type="date" id="prefDate" name="preferred_date" required class="input-glass w-full rounded-lg px-4 py-2 [color-scheme:dark]">
                    </div>

                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <label class="block text-xs text-slate-400 mb-1">Budget Threshold (INR)</label>
                            <div class="relative">
                                <span class="absolute left-4 top-2 text-slate-400">₹</span>
                                <input type="number" id="budget" name="budget_threshold" required class="input-glass w-full rounded-lg pl-8 pr-4 py-2" placeholder="5000">
                            </div>
                        </div>
                        <div>
                            <label class="block text-xs text-slate-400 mb-1">Flight Type / Preference</label>
                            <select id="flightType" name="flight_type" class="input-glass w-full rounded-lg px-3 py-2 text-xs bg-slate-900 border-slate-700">
                                <option value="ALL">All Flights (Direct & Layovers)</option>
                                <option value="DIRECT">Direct Flights Only</option>
                                <option value="LAYOVER">Layover Flights Only</option>
                            </select>
                        </div>
                    </div>
                    
                    <div class="flex items-center justify-between pt-2">
                        <label class="flex items-center cursor-pointer">
                            <div class="relative">
                                <input type="checkbox" id="activeToggle" name="active" value="1" checked class="sr-only">
                                <div class="block bg-slate-700 w-10 h-6 rounded-full transition-colors" id="toggleBg"></div>
                                <div class="dot absolute left-1 top-1 bg-white w-4 h-4 rounded-full transition-transform transform translate-x-4" id="toggleDot"></div>
                            </div>
                            <span class="ml-3 text-sm text-slate-300">Active Tracking</span>
                        </label>
                        <button type="submit" class="bg-primary hover:bg-indigo-500 text-white px-5 py-2 rounded-lg text-sm transition-colors shadow-lg shadow-indigo-500/30">
                            Save Profile
                        </button>
                    </div>
                </form>
                <div id="formMsg" class="text-xs text-green-400 mt-4 hidden">Configuration saved successfully!</div>
            </div>

            <!-- Active Profiles List -->
            <div class="glass-panel rounded-2xl p-6 max-h-[300px] overflow-y-auto">
                <h3 class="text-lg font-semibold mb-4 text-slate-200">Active Profiles</h3>
                <div id="profilesList" class="space-y-3">
                    <!-- Populated by JS -->
                    <div class="text-slate-400 text-sm text-center py-4">Loading profiles...</div>
                </div>
            </div>

            <!-- System Management & Reset Tools -->
            <div class="glass-panel rounded-2xl p-6 space-y-4">
                <h3 class="text-lg font-semibold text-slate-200 flex items-center gap-2">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-amber-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
                    </svg>
                    System Tools & Resets
                </h3>
                <p class="text-xs text-slate-400">Perform administrative resets and system maintenance.</p>
                
                <div class="space-y-3 pt-2">
                    <!-- Reset Profiles Button -->
                    <button type="button" onclick="confirmResetProfiles()" class="w-full bg-red-950/40 hover:bg-red-900/60 text-red-300 border border-red-800/50 p-3 rounded-xl text-xs font-medium transition-all flex items-center justify-between group shadow-sm">
                        <div class="flex items-center gap-2">
                            <span class="text-base">🧹</span>
                            <div class="text-left">
                                <div class="font-semibold text-red-200">Reset Profiles & Clear Logs</div>
                                <div class="text-[10px] text-red-400/80">Wipes search routes, prices & alert history</div>
                            </div>
                        </div>
                        <span class="text-slate-500 group-hover:text-red-300 transition-colors">Reset ↗</span>
                    </button>

                    <!-- Reset SMTP Button -->
                    <button type="button" onclick="confirmResetSmtp()" class="w-full bg-amber-950/40 hover:bg-amber-900/60 text-amber-300 border border-amber-800/50 p-3 rounded-xl text-xs font-medium transition-all flex items-center justify-between group shadow-sm">
                        <div class="flex items-center gap-2">
                            <span class="text-base">⚙️</span>
                            <div class="text-left">
                                <div class="font-semibold text-amber-200">Reset SMTP Configuration</div>
                                <div class="text-[10px] text-amber-400/80">Resets email settings in .env to defaults</div>
                            </div>
                        </div>
                        <span class="text-slate-500 group-hover:text-amber-300 transition-colors">Reset ↗</span>
                    </button>
                </div>
                <div id="resetStatusMsg" class="text-xs p-3 rounded-lg hidden"></div>
            </div>
        </div>

        <!-- Right Column: Dashboard Visualization -->
        <div class="lg:col-span-2 space-y-8">
            <!-- Chart Card -->
            <div class="glass-panel rounded-2xl p-6">
                <div class="flex justify-between items-end mb-6">
                    <div>
                        <h2 class="text-xl font-semibold">Cost Trend Analysis</h2>
                        <p class="text-slate-400 text-sm mt-1" id="chartSubtitle">Select a profile to view ±5 days trend</p>
                    </div>
                    <div class="text-right">
                        <p class="text-xs text-slate-400 mb-1">Target Budget</p>
                        <p class="text-xl font-bold text-green-400" id="displayBudget">₹0</p>
                    </div>
                </div>
                <div class="relative h-64 w-full">
                    <canvas id="trendChart"></canvas>
                </div>
            </div>

            <!-- Summary Table -->
            <div class="glass-panel rounded-2xl p-6">
                <div class="flex justify-between items-center mb-4">
                    <h3 class="text-lg font-semibold text-slate-200">Flight Details (Lowest Fares)</h3>
                </div>

                <!-- Filter & Sort Toolbar -->
                <div class="flex flex-wrap items-center justify-between gap-3 mb-4 bg-slate-800/40 p-3 rounded-xl border border-slate-700/50 text-xs">
                    <div class="flex items-center gap-2">
                        <span class="text-slate-400 font-medium">Sort By:</span>
                        <select id="sortOption" onchange="applyFiltersAndSort()" class="input-glass rounded-lg px-3 py-1.5 text-xs bg-slate-900 border-slate-700">
                            <option value="price_asc">Price: Low to High</option>
                            <option value="price_desc">Price: High to Low</option>
                            <option value="time_asc">Departure: Earliest</option>
                            <option value="time_desc">Departure: Latest</option>
                            <option value="date_asc">Date: Earliest</option>
                            <option value="airline_asc">Airline: A-Z</option>
                        </select>
                    </div>

                    <div class="flex items-center gap-2">
                        <span class="text-slate-400 font-medium">Airline:</span>
                        <select id="filterAirline" onchange="applyFiltersAndSort()" class="input-glass rounded-lg px-3 py-1.5 text-xs bg-slate-900 border-slate-700">
                            <option value="ALL">All Airlines</option>
                            <option value="IndiGo">IndiGo</option>
                            <option value="Air India">Air India</option>
                            <option value="Vistara">Vistara</option>
                            <option value="Akasa Air">Akasa Air</option>
                            <option value="SpiceJet">SpiceJet</option>
                        </select>
                    </div>

                    <div class="flex items-center gap-2">
                        <span class="text-slate-400 font-medium">Stops:</span>
                        <select id="filterStops" onchange="applyFiltersAndSort()" class="input-glass rounded-lg px-3 py-1.5 text-xs bg-slate-900 border-slate-700">
                            <option value="ALL">All Results</option>
                            <option value="DIRECT">Direct Only</option>
                            <option value="LAYOVER">Layovers Only</option>
                        </select>
                    </div>

                    <label class="flex items-center gap-2 cursor-pointer select-none">
                        <input type="checkbox" id="filterUnderBudget" onchange="applyFiltersAndSort()" class="rounded border-slate-700 bg-slate-900 text-primary focus:ring-primary h-4 w-4">
                        <span class="text-slate-300 font-medium">Under Budget Only</span>
                    </label>
                </div>

                <div class="overflow-x-auto">
                    <table class="w-full text-left text-sm text-slate-300">
                        <thead class="text-xs text-slate-400 uppercase bg-slate-800/50 rounded-lg">
                            <tr>
                                <th class="px-4 py-3 rounded-l-lg">Date</th>
                                <th class="px-4 py-3">Airline</th>
                                <th class="px-4 py-3">Time</th>
                                <th class="px-4 py-3">Price</th>
                                <th class="px-4 py-3 rounded-r-lg text-right">Action</th>
                            </tr>
                        </thead>
                        <tbody id="summaryTableBody">
                            <tr>
                                <td colspan="5" class="px-4 py-6 text-center text-slate-500">No data available. Run check or select profile.</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

    </main>

    <!-- Settings Modal -->
    <div id="settingsModal" class="fixed inset-0 bg-slate-950/80 backdrop-blur-md z-50 flex items-center justify-center p-4 hidden">
        <div class="glass-panel max-w-2xl w-full rounded-2xl p-6 relative border border-slate-700/80 shadow-2xl space-y-6 max-h-[90vh] overflow-y-auto">
            <!-- Modal Header -->
            <div class="flex justify-between items-center border-b border-slate-700/60 pb-4">
                <div class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-xl bg-indigo-600/20 border border-indigo-500/30 flex items-center justify-center text-indigo-400">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                        </svg>
                    </div>
                    <div>
                        <h2 class="text-xl font-bold text-white">Environment Settings (.env)</h2>
                        <p class="text-xs text-slate-400">Configure SMTP email alerts, Telegram integration & database connections</p>
                    </div>
                </div>
                <button onclick="closeSettingsModal()" class="text-slate-400 hover:text-white p-2 rounded-lg hover:bg-slate-800 transition-colors">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                    </svg>
                </button>
            </div>

            <!-- Tab Navigation -->
            <div class="flex border-b border-slate-700/60 gap-4 text-sm font-medium">
                <button id="tabBtnSmtp" type="button" onclick="switchSettingsTab('smtp')" class="pb-3 border-b-2 border-primary text-primary px-1 flex items-center gap-2">
                    📧 SMTP Email
                </button>
                <button id="tabBtnTelegram" type="button" onclick="switchSettingsTab('telegram')" class="pb-3 border-b-2 border-transparent text-slate-400 hover:text-slate-200 px-1 flex items-center gap-2">
                    📱 Telegram Alerts
                </button>
                <button id="tabBtnDb" type="button" onclick="switchSettingsTab('db')" class="pb-3 border-b-2 border-transparent text-slate-400 hover:text-slate-200 px-1 flex items-center gap-2">
                    🗄️ Database
                </button>
            </div>

            <!-- Settings Form -->
            <form id="envSettingsForm" onsubmit="saveEnvSettings(event)">
                
                <!-- SMTP Tab Content -->
                <div id="tabContentSmtp" class="space-y-4">
                    <div class="flex justify-between items-center bg-slate-800/40 p-3 rounded-xl border border-slate-700/50">
                        <span class="text-xs text-slate-300">Quick Provider Presets:</span>
                        <div class="flex gap-2">
                            <button type="button" onclick="applySmtpPreset('gmail')" class="text-xs bg-indigo-600/30 hover:bg-indigo-600/50 text-indigo-300 border border-indigo-500/40 px-3 py-1 rounded-md transition-all">
                                Gmail (smtp.gmail.com)
                            </button>
                            <button type="button" onclick="applySmtpPreset('outlook')" class="text-xs bg-blue-600/30 hover:bg-blue-600/50 text-blue-300 border border-blue-500/40 px-3 py-1 rounded-md transition-all">
                                Outlook (smtp.office365.com)
                            </button>
                        </div>
                    </div>

                    <div class="grid grid-cols-3 gap-4">
                        <div class="col-span-2">
                            <label class="block text-xs text-slate-400 mb-1">SMTP Server</label>
                            <input type="text" id="env_SMTP_SERVER" name="SMTP_SERVER" class="input-glass w-full rounded-lg px-4 py-2" placeholder="smtp.gmail.com">
                        </div>
                        <div>
                            <label class="block text-xs text-slate-400 mb-1">SMTP Port</label>
                            <input type="number" id="env_SMTP_PORT" name="SMTP_PORT" class="input-glass w-full rounded-lg px-4 py-2" placeholder="587">
                        </div>
                    </div>

                    <div>
                        <label class="block text-xs text-slate-400 mb-1">SMTP Username / Sender Email</label>
                        <input type="email" id="env_SMTP_USER" name="SMTP_USER" class="input-glass w-full rounded-lg px-4 py-2" placeholder="your_email@gmail.com">
                    </div>

                    <div>
                        <label class="block text-xs text-slate-400 mb-1">SMTP Password / App Password</label>
                        <div class="relative">
                            <input type="password" id="env_SMTP_PASS" name="SMTP_PASS" class="input-glass w-full rounded-lg px-4 py-2 pr-10" placeholder="16-character app password">
                            <button type="button" onclick="togglePasswordVisibility('env_SMTP_PASS')" class="absolute right-3 top-2.5 text-slate-400 hover:text-white">
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                                </svg>
                            </button>
                        </div>
                    </div>

                    <div>
                        <label class="block text-xs text-slate-400 mb-1">Alert Recipient Email</label>
                        <input type="email" id="env_ALERT_RECIPIENT" name="ALERT_RECIPIENT" class="input-glass w-full rounded-lg px-4 py-2" placeholder="alert_recipient@example.com">
                    </div>

                    <!-- SMTP Test Connection Box -->
                    <div class="pt-2 flex items-center justify-between">
                        <button type="button" onclick="testSmtpConnection()" id="smtpTestBtn" class="bg-slate-800 hover:bg-slate-700 text-indigo-400 border border-indigo-500/40 px-4 py-2 rounded-lg text-xs font-medium transition-all flex items-center gap-2">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                            </svg>
                            <span id="smtpTestBtnText">Test SMTP Connection</span>
                        </button>
                    </div>
                    <div id="smtpTestResult" class="text-xs p-3 rounded-lg hidden"></div>
                </div>

                <!-- Telegram Tab Content -->
                <div id="tabContentTelegram" class="space-y-4 hidden">
                    <div>
                        <label class="block text-xs text-slate-400 mb-1">Telegram Bot Token</label>
                        <input type="text" id="env_TELEGRAM_BOT_TOKEN" name="TELEGRAM_BOT_TOKEN" class="input-glass w-full rounded-lg px-4 py-2" placeholder="123456789:ABCdefGHIjklMNOpqrsTUVwxy-z">
                    </div>
                    <div>
                        <label class="block text-xs text-slate-400 mb-1">Telegram Chat ID</label>
                        <input type="text" id="env_TELEGRAM_CHAT_ID" name="TELEGRAM_CHAT_ID" class="input-glass w-full rounded-lg px-4 py-2" placeholder="-100123456789">
                    </div>
                </div>

                <!-- DB Tab Content -->
                <div id="tabContentDb" class="space-y-4 hidden">
                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <label class="block text-xs text-slate-400 mb-1">Database Host</label>
                            <input type="text" id="env_DB_HOST" name="DB_HOST" class="input-glass w-full rounded-lg px-4 py-2" placeholder="localhost">
                        </div>
                        <div>
                            <label class="block text-xs text-slate-400 mb-1">Database Name</label>
                            <input type="text" id="env_DB_NAME" name="DB_NAME" class="input-glass w-full rounded-lg px-4 py-2" placeholder="flight_tracker_db">
                        </div>
                    </div>
                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <label class="block text-xs text-slate-400 mb-1">Database Username</label>
                            <input type="text" id="env_DB_USER" name="DB_USER" class="input-glass w-full rounded-lg px-4 py-2" placeholder="root">
                        </div>
                        <div>
                            <label class="block text-xs text-slate-400 mb-1">Database Password</label>
                            <input type="password" id="env_DB_PASS" name="DB_PASS" class="input-glass w-full rounded-lg px-4 py-2" placeholder="(leave blank if none)">
                        </div>
                    </div>
                </div>

                <!-- Footer Actions -->
                <div class="pt-6 border-t border-slate-700/60 flex items-center justify-between">
                    <div id="envFormMsg" class="text-xs text-green-400 hidden">Environment settings saved!</div>
                    <div class="flex gap-3 ml-auto">
                        <button type="button" onclick="closeSettingsModal()" class="px-4 py-2 rounded-lg text-sm text-slate-400 hover:text-white hover:bg-slate-800 transition-colors">
                            Cancel
                        </button>
                        <button type="submit" id="saveEnvBtn" class="bg-primary hover:bg-indigo-500 text-white px-6 py-2 rounded-lg text-sm font-medium transition-all shadow-lg shadow-indigo-500/30">
                            Save Settings
                        </button>
                    </div>
                </div>
            </form>
        </div>
    </div>

    <script>
        let trendChartInstance = null;
        let currentConfigId = null;

        // Toggle UI logic
        const toggleInput = document.getElementById('activeToggle');
        const toggleBg = document.getElementById('toggleBg');
        const toggleDot = document.getElementById('toggleDot');
        
        toggleInput.addEventListener('change', function() {
            if (this.checked) {
                toggleBg.classList.replace('bg-slate-700', 'bg-primary');
                toggleDot.classList.replace('translate-x-0', 'translate-x-4');
            } else {
                toggleBg.classList.replace('bg-primary', 'bg-slate-700');
                toggleDot.classList.replace('translate-x-4', 'translate-x-0');
            }
        });

        // Initialize toggle visual state
        toggleBg.classList.replace('bg-slate-700', 'bg-primary');

        function resetConfigForm() {
            document.getElementById('configId').value = '0';
            document.getElementById('departure').value = '';
            document.getElementById('arrival').value = '';
            document.getElementById('prefDate').value = '';
            document.getElementById('budget').value = '';
            if (document.getElementById('flightType')) document.getElementById('flightType').value = 'ALL';
            document.getElementById('activeToggle').checked = true;
            toggleInput.dispatchEvent(new Event('change'));
            document.getElementById('formTitle').innerText = 'New Flight Route';
            document.getElementById('departure').focus();
        }

        async function loadProfiles() {
            try {
                const res = await fetch('api/save_config.php');
                const json = await res.json();
                const container = document.getElementById('profilesList');
                container.innerHTML = '';
                
                if (json.success && json.data.length > 0) {
                    json.data.forEach(p => {
                        const div = document.createElement('div');
                        const statusColor = p.active == 1 ? 'text-green-400' : 'text-slate-500';
                        const dotColor = p.active == 1 ? 'bg-green-400' : 'bg-slate-500';
                        const isSelected = currentConfigId == p.id;
                        const typeLabel = p.flight_type == 'DIRECT' ? 'Direct Only' : (p.flight_type == 'LAYOVER' ? 'Layovers Only' : 'All Flights');
                        div.className = `p-3 rounded-xl border border-slate-700/50 hover:border-primary/50 cursor-pointer transition-all bg-slate-800/30 flex justify-between items-center ${isSelected ? 'border-primary ring-1 ring-primary/30 bg-slate-800/70' : ''}`;
                        div.onclick = () => {
                            currentConfigId = p.id;
                            loadProfileData(p);
                            loadTrendData(p.id);
                            loadProfiles();
                        };
                        div.innerHTML = `
                            <div class="flex-1">
                                <div class="font-semibold text-white tracking-wider flex items-center gap-2">
                                    ${p.departure_city} → ${p.arrival_city}
                                    <span class="text-[10px] bg-slate-700 text-slate-300 px-2 py-0.5 rounded-full font-normal">${typeLabel}</span>
                                </div>
                                <div class="text-xs text-slate-400 mt-1">${p.preferred_date}</div>
                            </div>
                            <div class="text-right flex items-center gap-3">
                                <div>
                                    <div class="font-mono text-sm mb-1">₹${p.budget_threshold}</div>
                                    <div class="flex items-center justify-end gap-1 text-[10px] ${statusColor}">
                                        <span class="w-2 h-2 rounded-full ${dotColor}"></span>
                                        ${p.active == 1 ? 'ACTIVE' : 'PAUSED'}
                                    </div>
                                </div>
                                <button type="button" onclick="deleteConfig(${p.id}, event)" title="Delete Route" class="text-slate-500 hover:text-red-400 p-1.5 rounded-lg hover:bg-slate-700/50 transition-colors">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                    </svg>
                                </button>
                            </div>
                        `;
                        container.appendChild(div);
                    });
                    
                    // Auto-load first profile if none selected
                    if (!currentConfigId && json.data.length > 0) {
                        currentConfigId = json.data[0].id;
                        loadProfileData(json.data[0]);
                        loadTrendData(json.data[0].id);
                    }
                } else {
                    container.innerHTML = '<div class="text-slate-500 text-sm text-center py-4">No tracking profiles yet. Click "+ New Route" above to add one.</div>';
                }
            } catch (e) {
                console.error("Failed to load profiles", e);
            }
        }

        function loadProfileData(p) {
            document.getElementById('configId').value = p.id;
            document.getElementById('departure').value = p.departure_city;
            document.getElementById('arrival').value = p.arrival_city;
            document.getElementById('prefDate').value = p.preferred_date;
            document.getElementById('budget').value = p.budget_threshold;
            if (document.getElementById('flightType')) document.getElementById('flightType').value = p.flight_type || 'ALL';
            document.getElementById('activeToggle').checked = (p.active == 1);
            toggleInput.dispatchEvent(new Event('change'));
            document.getElementById('formTitle').innerText = `Edit Route (${p.departure_city} → ${p.arrival_city})`;
        }

        async function deleteConfig(id, e) {
            e.stopPropagation(); // prevent card click
            if (!confirm("Are you sure you want to delete this flight route?")) return;
            
            try {
                const formData = new FormData();
                formData.append('id', id);
                const res = await fetch('api/delete_config.php', {
                    method: 'POST',
                    body: formData
                });
                const json = await res.json();
                if (json.success) {
                    if (currentConfigId == id) {
                        currentConfigId = null;
                        resetConfigForm();
                    }
                    loadProfiles();
                } else {
                    alert("Error deleting profile: " + json.error);
                }
            } catch (err) {
                alert("Failed to delete profile");
            }
        }

        async function saveConfig(e) {
            e.preventDefault();
            const form = e.target;
            const formData = new FormData(form);
            
            // Handle unchecked checkbox not sending value
            if(!document.getElementById('activeToggle').checked) {
                formData.append('active', '0');
            }
            
            try {
                const res = await fetch('api/save_config.php', {
                    method: 'POST',
                    body: formData
                });
                const json = await res.json();
                if(json.success) {
                    const msg = document.getElementById('formMsg');
                    msg.classList.remove('hidden');
                    setTimeout(() => msg.classList.add('hidden'), 3000);
                    
                    if(!document.getElementById('configId').value || document.getElementById('configId').value == '0') {
                        document.getElementById('configId').value = json.id;
                        currentConfigId = json.id;
                    }
                    loadProfiles();
                } else {
                    alert("Error saving: " + json.error);
                }
            } catch (err) {
                alert("Request failed");
            }
        }

        async function triggerScrape() {
            const btn = document.getElementById('scrapeBtn');
            const btnText = document.getElementById('scrapeBtnText');
            btn.classList.add('opacity-75', 'cursor-not-allowed');
            btnText.innerText = "Running Check...";
            
            try {
                const res = await fetch('api/trigger_scrape.php');
                const json = await res.json();
                console.log(json.output);
                
                if (currentConfigId) {
                    loadTrendData(currentConfigId); // Refresh data
                }
            } catch (e) {
                console.error("Scrape failed", e);
            } finally {
                btn.classList.remove('opacity-75', 'cursor-not-allowed');
                btnText.innerText = "Run Manual Check";
            }
        }

        let rawFlightData = [];
        let currentBudgetThreshold = 0;
        let currentDepCity = '';
        let currentArrCity = '';

        function cleanAirportInput(el) {
            if (!el) return;
            let val = el.value.trim().toUpperCase();
            if (val.length > 3) {
                const match = val.match(/^([A-Z]{3})/);
                if (match) {
                    el.value = match[1];
                }
            } else {
                el.value = val;
            }
        }

        async function loadTrendData(configId) {
            try {
                const res = await fetch('api/get_trend_data.php?config_id=' + configId);
                const json = await res.json();
                
                if(json.success) {
                    document.getElementById('chartSubtitle').innerText = `Data for target date: ${json.preferred_date}`;
                    document.getElementById('displayBudget').innerText = `₹${json.budget_threshold}`;
                    rawFlightData = json.data || [];
                    currentBudgetThreshold = json.budget_threshold;
                    currentDepCity = json.departure_city;
                    currentArrCity = json.arrival_city;
                    
                    renderChart(json.data, json.budget_threshold, json.preferred_date);
                    applyFiltersAndSort();
                }
            } catch (e) {
                console.error("Failed to load trend data", e);
            }
        }

        function applyFiltersAndSort() {
            let filtered = [...rawFlightData];
            
            // 1. Filter by Airline
            const selectedAirline = document.getElementById('filterAirline')?.value || 'ALL';
            if (selectedAirline !== 'ALL') {
                filtered = filtered.filter(d => d.airline.toLowerCase() === selectedAirline.toLowerCase());
            }
            
            // 2. Filter by Stops / Layover Preference
            const selectedStops = document.getElementById('filterStops')?.value || 'ALL';
            if (selectedStops === 'DIRECT') {
                filtered = filtered.filter(d => d.is_direct == 1 || (d.stops_info && d.stops_info.toLowerCase().includes('direct')));
            } else if (selectedStops === 'LAYOVER') {
                filtered = filtered.filter(d => d.is_direct == 0 || (d.stops_info && !d.stops_info.toLowerCase().includes('direct')));
            }

            // 3. Filter Under Budget Only
            const underBudgetOnly = document.getElementById('filterUnderBudget')?.checked;
            if (underBudgetOnly) {
                filtered = filtered.filter(d => parseFloat(d.min_price) <= currentBudgetThreshold);
            }
            
            // 4. Sort
            const sortOption = document.getElementById('sortOption')?.value || 'price_asc';
            filtered.sort((a, b) => {
                if (sortOption === 'price_asc') return parseFloat(a.min_price) - parseFloat(b.min_price);
                if (sortOption === 'price_desc') return parseFloat(b.min_price) - parseFloat(a.min_price);
                if (sortOption === 'time_asc') return a.departure_time.localeCompare(b.departure_time);
                if (sortOption === 'time_desc') return b.departure_time.localeCompare(a.departure_time);
                if (sortOption === 'date_asc') return a.flight_date.localeCompare(b.flight_date);
                if (sortOption === 'airline_asc') return a.airline.localeCompare(b.airline);
                return 0;
            });
            
            renderTable(filtered, currentBudgetThreshold, currentDepCity, currentArrCity);
        }

        function renderChart(data, budget, targetDate) {
            const ctx = document.getElementById('trendChart').getContext('2d');
            
            const labels = data.map(d => d.flight_date.substring(5)); // MM-DD
            const prices = data.map(d => parseFloat(d.min_price));
            
            // Create gradient
            let gradient = ctx.createLinearGradient(0, 0, 0, 400);
            gradient.addColorStop(0, 'rgba(99, 102, 241, 0.5)');   
            gradient.addColorStop(1, 'rgba(99, 102, 241, 0.0)');

            const pointColors = data.map(d => parseFloat(d.min_price) <= budget ? '#4ade80' : '#6366f1');
            const pointRadii = data.map(d => d.flight_date === targetDate ? 6 : 4);

            if(trendChartInstance) {
                trendChartInstance.destroy();
            }

            // Annotation for budget line requires a plugin, we'll draw it manually via dataset for simplicity, 
            // or just rely on coloring. Let's add a horizontal dataset for the budget limit.
            const budgetLine = Array(data.length).fill(budget);

            trendChartInstance = new Chart(ctx, {
                type: 'line',
                data: {
                    labels: labels,
                    datasets: [
                        {
                            label: 'Lowest Price',
                            data: prices,
                            borderColor: '#6366f1',
                            backgroundColor: gradient,
                            borderWidth: 2,
                            pointBackgroundColor: pointColors,
                            pointBorderColor: '#fff',
                            pointRadius: pointRadii,
                            pointHoverRadius: 8,
                            fill: true,
                            tension: 0.4
                        },
                        {
                            label: 'Budget Threshold',
                            data: budgetLine,
                            borderColor: '#ef4444',
                            borderWidth: 1,
                            borderDash: [5, 5],
                            pointRadius: 0,
                            fill: false
                        }
                    ]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { display: false },
                        tooltip: {
                            mode: 'index',
                            intersect: false,
                            backgroundColor: 'rgba(15, 23, 42, 0.9)',
                            titleColor: '#fff',
                            bodyColor: '#cbd5e1',
                            borderColor: 'rgba(255,255,255,0.1)',
                            borderWidth: 1
                        }
                    },
                    scales: {
                        y: {
                            grid: { color: 'rgba(255, 255, 255, 0.05)' },
                            ticks: { color: '#94a3b8', font: {family: 'Outfit'} }
                        },
                        x: {
                            grid: { display: false },
                            ticks: { color: '#94a3b8', font: {family: 'Outfit'} }
                        }
                    },
                    interaction: {
                        mode: 'nearest',
                        axis: 'x',
                        intersect: false
                    }
                }
            });
        }

        function renderTable(data, budget, depCity, arrCity) {
            const tbody = document.getElementById('summaryTableBody');
            tbody.innerHTML = '';
            
            if (!data || data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5" class="px-4 py-6 text-center text-slate-500">No flight details match the selected filters.</td></tr>';
                return;
            }

            data.forEach(d => {
                const isUnderBudget = parseFloat(d.min_price) <= budget;
                const priceClass = isUnderBudget ? 'text-green-400 font-bold' : 'text-slate-300';
                
                // Parse date components for target OTAs
                const dateParts = d.flight_date.split('-');
                const yyyy = dateParts[0];
                const mm = dateParts[1];
                const dd = dateParts[2];
                const yy = yyyy.substring(2);
                
                const dCity = (depCity || 'DEL').trim().toUpperCase();
                const aCity = (arrCity || 'BOM').trim().toUpperCase();
                
                // 1. Google Flights Link (Explicit one-way search)
                const googleQuery = encodeURIComponent(`one-way flights from ${dCity} to ${aCity} on ${d.flight_date} ${d.airline}`);
                const googleFlightsUrl = `https://www.google.com/travel/flights?q=${googleQuery}`;
                
                // 2. MakeMyTrip Link (DD/MM/YYYY format required)
                const mmtDate = `${dd}/${mm}/${yyyy}`;
                const mmtUrl = `https://www.makemytrip.com/flight/search?itinerary=${dCity}-${aCity}-${mmtDate}&tripType=O&paxType=A-1_C-0_I-0&intl=false&cabinClass=E`;
                
                // 3. EaseMyTrip Link
                const emtUrl = `https://flight.easemytrip.com/FlightList/Index?srch=${dCity}-${aCity}-${mmtDate}&px=1-0-0&c=E&m=0&bType=SEARCH`;
                
                // 4. Skyscanner Link (YYMMDD format)
                const skyscannerUrl = `https://www.skyscanner.co.in/transport/flights/${dCity.toLowerCase()}/${aCity.toLowerCase()}/${yy}${mm}${dd}/?adultsv2=1&cabinclass=economy`;
                
                const tr = document.createElement('tr');
                tr.className = 'border-b border-slate-700/50 hover:bg-slate-800/30 transition-colors';
                tr.innerHTML = `
                    <td class="px-4 py-3 whitespace-nowrap font-medium text-slate-200">${d.flight_date}</td>
                    <td class="px-4 py-3">
                        <div class="flex items-center gap-2">
                            <div class="w-6 h-6 rounded-full bg-slate-700 flex items-center justify-center text-[10px] font-bold text-white">
                                ${d.airline.substring(0,2).toUpperCase()}
                            </div>
                            <div>
                                <div class="flex items-center gap-1.5">
                                    <span class="text-slate-200 font-medium">${d.airline}</span>
                                    <span class="text-xs text-slate-500 font-mono">${d.flight_number}</span>
                                </div>
                                <div class="mt-0.5">
                                    ${(d.is_direct == 1 || (d.stops_info && d.stops_info.toLowerCase().includes('direct')))
                                        ? '<span class="text-[10px] bg-emerald-950/60 text-emerald-400 border border-emerald-800/40 px-2 py-0.5 rounded-full font-medium">Direct</span>'
                                        : `<span class="text-[10px] bg-amber-950/60 text-amber-400 border border-amber-800/40 px-2 py-0.5 rounded-full font-medium">${d.stops_info || '1 Stop'}</span>`
                                    }
                                </div>
                            </div>
                        </div>
                    </td>
                    <td class="px-4 py-3 text-slate-400 text-sm font-mono">
                        ${d.departure_time.substring(0,5)} - ${d.arrival_time.substring(0,5)}
                    </td>
                    <td class="px-4 py-3 font-mono">
                        <div class="${priceClass} text-base font-bold">₹${parseInt(d.min_price).toLocaleString('en-IN')}</div>
                        <div class="text-[10px] text-emerald-400 font-sans flex items-center gap-1 mt-0.5">
                            <span class="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse"></span>
                            Cheapest Deal
                        </div>
                    </td>
                    <td class="px-4 py-3 text-right whitespace-nowrap">
                        <div class="inline-flex items-center gap-1.5">
                            <a href="${mmtUrl}" target="_blank" title="Book cheapest deal on MakeMyTrip (${dCity} -> ${aCity} on ${mmtDate})" class="inline-flex items-center gap-1 text-xs bg-emerald-600/30 hover:bg-emerald-600/70 text-emerald-300 hover:text-white border border-emerald-500/40 px-3 py-1.5 rounded-lg transition-all font-semibold shadow-sm hover:scale-105 transform">
                                <span>Book on MMT</span>
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
                                </svg>
                            </a>
                            <a href="${googleFlightsUrl}" target="_blank" title="Verify / Book ${d.airline} on Google Flights" class="inline-flex items-center gap-1 text-xs bg-indigo-600/30 hover:bg-indigo-600/70 text-indigo-300 hover:text-white border border-indigo-500/40 px-2.5 py-1.5 rounded-lg transition-all font-medium shadow-sm hover:scale-105 transform">
                                <span>Google Flights</span>
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
                                </svg>
                            </a>
                            <a href="${emtUrl}" target="_blank" title="Compare direct search on EaseMyTrip" class="inline-flex items-center text-xs bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white border border-slate-700 px-2.5 py-1.5 rounded-lg transition-all font-medium">
                                EMT ↗
                            </a>
                        </div>
                    </td>
                `;
                tbody.appendChild(tr);
            });
        }

        // Settings Modal JS Logic
        async function loadSettings() {
            try {
                const res = await fetch('api/env_config.php');
                const json = await res.json();
                if (json.success && json.data) {
                    for (const [key, val] of Object.entries(json.data)) {
                        const el = document.getElementById('env_' + key);
                        if (el) el.value = val;
                    }
                }
            } catch (e) {
                console.error("Failed to load .env settings", e);
            }
        }

        async function openSettingsModal() {
            await loadSettings();
            document.getElementById('settingsModal').classList.remove('hidden');
        }

        function closeSettingsModal() {
            document.getElementById('settingsModal').classList.add('hidden');
            document.getElementById('smtpTestResult').classList.add('hidden');
            document.getElementById('envFormMsg').classList.add('hidden');
        }

        function switchSettingsTab(tabName) {
            const tabs = ['smtp', 'telegram', 'db'];
            tabs.forEach(t => {
                const content = document.getElementById('tabContent' + t.charAt(0).toUpperCase() + t.slice(1));
                const btn = document.getElementById('tabBtn' + t.charAt(0).toUpperCase() + t.slice(1));
                if (t === tabName) {
                    content.classList.remove('hidden');
                    btn.classList.add('border-primary', 'text-primary');
                    btn.classList.remove('border-transparent', 'text-slate-400');
                } else {
                    content.classList.add('hidden');
                    btn.classList.remove('border-primary', 'text-primary');
                    btn.classList.add('border-transparent', 'text-slate-400');
                }
            });
        }

        function togglePasswordVisibility(fieldId) {
            const input = document.getElementById(fieldId);
            input.type = input.type === 'password' ? 'text' : 'password';
        }

        function applySmtpPreset(provider) {
            if (provider === 'gmail') {
                document.getElementById('env_SMTP_SERVER').value = 'smtp.gmail.com';
                document.getElementById('env_SMTP_PORT').value = '587';
            } else if (provider === 'outlook') {
                document.getElementById('env_SMTP_SERVER').value = 'smtp.office365.com';
                document.getElementById('env_SMTP_PORT').value = '587';
            }
        }

        async function testSmtpConnection() {
            const btn = document.getElementById('smtpTestBtn');
            const btnText = document.getElementById('smtpTestBtnText');
            const resultBox = document.getElementById('smtpTestResult');
            
            btn.classList.add('opacity-75', 'cursor-not-allowed');
            btnText.innerText = "Testing SMTP...";
            resultBox.classList.add('hidden');
            
            const formData = new FormData();
            formData.append('SMTP_SERVER', document.getElementById('env_SMTP_SERVER').value);
            formData.append('SMTP_PORT', document.getElementById('env_SMTP_PORT').value);
            formData.append('SMTP_USER', document.getElementById('env_SMTP_USER').value);
            formData.append('SMTP_PASS', document.getElementById('env_SMTP_PASS').value);
            formData.append('ALERT_RECIPIENT', document.getElementById('env_ALERT_RECIPIENT').value);

            try {
                const res = await fetch('api/test_smtp.php', {
                    method: 'POST',
                    body: formData
                });
                const json = await res.json();
                
                resultBox.classList.remove('hidden', 'bg-red-900/40', 'border-red-500/40', 'text-red-300', 'bg-green-900/40', 'border-green-500/40', 'text-green-300');
                if (json.success) {
                    resultBox.classList.add('bg-green-900/40', 'border', 'border-green-500/40', 'text-green-300');
                    resultBox.innerText = "✅ " + json.message;
                } else {
                    resultBox.classList.add('bg-red-900/40', 'border', 'border-red-500/40', 'text-red-300');
                    resultBox.innerText = "❌ SMTP Error: " + json.error;
                }
            } catch (e) {
                resultBox.classList.remove('hidden');
                resultBox.classList.add('bg-red-900/40', 'border', 'border-red-500/40', 'text-red-300');
                resultBox.innerText = "❌ Connection test request failed.";
            } finally {
                btn.classList.remove('opacity-75', 'cursor-not-allowed');
                btnText.innerText = "Test SMTP Connection";
            }
        }

        async function saveEnvSettings(e) {
            e.preventDefault();
            const form = e.target;
            const formData = new FormData(form);
            const msg = document.getElementById('envFormMsg');
            
            try {
                const res = await fetch('api/env_config.php', {
                    method: 'POST',
                    body: formData
                });
                const json = await res.json();
                if (json.success) {
                    msg.innerText = "✅ " + json.message;
                    msg.classList.remove('hidden');
                    setTimeout(() => msg.classList.add('hidden'), 3500);
                } else {
                    alert("Error saving .env settings: " + json.error);
                }
            } catch (err) {
                alert("Request failed to save settings");
            }
        }

        async function confirmResetProfiles() {
            if (!confirm("⚠️ WARNING: This will permanently delete ALL flight search routes, price history, and alert logs from the database.\n\nAre you sure you want to proceed?")) {
                return;
            }

            const statusBox = document.getElementById('resetStatusMsg');
            statusBox.classList.add('hidden');

            try {
                const formData = new FormData();
                formData.append('action', 'reset_profiles');
                const res = await fetch('api/reset_system.php', {
                    method: 'POST',
                    body: formData
                });
                const json = await res.json();
                statusBox.classList.remove('hidden', 'bg-red-900/40', 'border-red-500/40', 'text-red-300', 'bg-green-900/40', 'border-green-500/40', 'text-green-300');
                if (json.success) {
                    statusBox.classList.add('bg-green-900/40', 'border', 'border-green-500/40', 'text-green-300');
                    statusBox.innerText = "✅ " + json.message;
                    currentConfigId = null;
                    resetConfigForm();
                    loadProfiles();
                    document.getElementById('summaryTableBody').innerHTML = '<tr><td colspan="5" class="px-4 py-6 text-center text-slate-500">No data available. Run check or select profile.</td></tr>';
                    if (trendChartInstance) trendChartInstance.destroy();
                } else {
                    statusBox.classList.add('bg-red-900/40', 'border', 'border-red-500/40', 'text-red-300');
                    statusBox.innerText = "❌ Reset Error: " + json.error;
                }
            } catch (e) {
                alert("Reset request failed");
            }
        }

        async function confirmResetSmtp() {
            if (!confirm("⚠️ WARNING: This will reset your SMTP credentials in .env back to default template placeholders.\n\nAre you sure you want to reset SMTP configuration?")) {
                return;
            }

            const statusBox = document.getElementById('resetStatusMsg');
            statusBox.classList.add('hidden');

            try {
                const formData = new FormData();
                formData.append('action', 'reset_smtp');
                const res = await fetch('api/reset_system.php', {
                    method: 'POST',
                    body: formData
                });
                const json = await res.json();
                statusBox.classList.remove('hidden', 'bg-red-900/40', 'border-red-500/40', 'text-red-300', 'bg-green-900/40', 'border-green-500/40', 'text-green-300');
                if (json.success) {
                    statusBox.classList.add('bg-green-900/40', 'border', 'border-green-500/40', 'text-green-300');
                    statusBox.innerText = "✅ " + json.message;
                    loadSettings();
                } else {
                    statusBox.classList.add('bg-red-900/40', 'border', 'border-red-500/40', 'text-red-300');
                    statusBox.innerText = "❌ Reset Error: " + json.error;
                }
            } catch (e) {
                alert("Reset SMTP request failed");
            }
        }

        // Init
        document.addEventListener('DOMContentLoaded', () => {
            loadProfiles();
            loadSettings();
        });

    </script>
</body>
</html>

