package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Contestant
import com.example.data.PageantRepository
import com.example.data.VoteTransaction
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

// Representation of the payment process states
sealed interface PaymentUiState {
    object Idle : PaymentUiState
    data class Initiated(val contestant: Contestant, val votesToBuy: Int, val amount: Double, val phoneNumber: String, val provider: String) : PaymentUiState
    data class AwaitingPin(val transactionId: String, val contestant: Contestant, val votesToBuy: Int, val amount: Double, val phoneNumber: String, val provider: String, val pinInput: String = "") : PaymentUiState
    data class AwaitingOtp(val transactionId: String, val contestant: Contestant, val votesToBuy: Int, val amount: Double, val phoneNumber: String, val provider: String, val otpCode: String, val otpInput: String = "", val errorMsg: String? = null) : PaymentUiState
    data class Processing(val statusMsg: String) : PaymentUiState
    data class Success(val transaction: VoteTransaction) : PaymentUiState
    data class Error(val message: String) : PaymentUiState
}

class PageantViewModel(private val repository: PageantRepository) : ViewModel() {

    // UI state for navigation tab
    private val _currentTab = MutableStateFlow(PageantTab.CONTESTANTS)
    val currentTab: StateFlow<PageantTab> = _currentTab.asStateFlow()

    // Selected contestant for details sheet
    private val _selectedContestant = MutableStateFlow<Contestant?>(null)
    val selectedContestant: StateFlow<Contestant?> = _selectedContestant.asStateFlow()

    // Selected contestant specifically for voting flow
    private val _votingContestant = MutableStateFlow<Contestant?>(null)
    val votingContestant: StateFlow<Contestant?> = _votingContestant.asStateFlow()

    // Mobile Money payment state
    private val _paymentState = MutableStateFlow<PaymentUiState>(PaymentUiState.Idle)
    val paymentState: StateFlow<PaymentUiState> = _paymentState.asStateFlow()

    // Flow for contestants list, automatically updated by Room database Flow
    val contestants: StateFlow<List<Contestant>> = repository.contestants
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Flow for transactions log, automatically updated by Room database Flow
    val transactions: StateFlow<List<VoteTransaction>> = repository.transactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        seedContestantsIfEmpty()
    }

    private fun seedContestantsIfEmpty() {
        viewModelScope.launch {
            if (!repository.hasContestants()) {
                val seedData = listOf(
                    Contestant(
                        name = "Aisha Keita",
                        candidateNumber = "001",
                        age = 23,
                        region = "West Africa",
                        platform = "Education for All",
                        biography = "Aisha is a passionate advocate for girls' education across West Africa. Having overcome significant challenges to complete her computer science studies, she believes that empowering young women with digital literacy is the ultimate catalyst for systemic equality and economic growth.",
                        votes = 1250,
                        imageResName = "img_contestant_1"
                    ),
                    Contestant(
                        name = "Mei-Ling Zhou",
                        candidateNumber = "002",
                        age = 22,
                        region = "East Asia",
                        platform = "Mental Health Access",
                        biography = "Mei-Ling is an aspiring clinical psychologist dedicated to erasing cultural taboos surrounding mental health in East Asian communities. She actively designs web platforms that offer anonymous, multi-lingual peer counseling and emotional resources for high school students.",
                        votes = 1480,
                        imageResName = "img_contestant_2"
                    ),
                    Contestant(
                        name = "Isabella Silva",
                        candidateNumber = "003",
                        age = 24,
                        region = "Latin America",
                        platform = "Eco-Sustainability & Green Cities",
                        biography = "Isabella is an environmental activist from Rio. She spearheads grassroots community composting projects, promotes recycling programs in under-served neighborhoods, and has organized urban planting drives responsible for over 5,000 native tree seedlings.",
                        votes = 1620,
                        imageResName = "img_contestant_3"
                    ),
                    Contestant(
                        name = "Zanele Ndlovu",
                        candidateNumber = "004",
                        age = 25,
                        region = "Southern Africa",
                        platform = "Women in STEM",
                        biography = "Zanele is a software developer mentoring rural women entering tech fields. She curates local programming workshops and coding clubs for kids, with a long-term dream of building solar-powered mobile classrooms to bridge the digital divide in marginalized villages.",
                        votes = 1100,
                        imageResName = "img_contestant_4"
                    )
                )
                repository.addContestants(seedData)
            }
        }
    }

    fun selectTab(tab: PageantTab) {
        _currentTab.value = tab
    }

    fun viewContestantDetails(contestant: Contestant?) {
        _selectedContestant.value = contestant
    }

    fun updateContestant(contestant: Contestant) {
        viewModelScope.launch {
            repository.updateContestant(contestant)
            if (_selectedContestant.value?.id == contestant.id) {
                _selectedContestant.value = contestant
            }
        }
    }

    fun startVotingFlow(contestant: Contestant) {
        _votingContestant.value = contestant
        _paymentState.value = PaymentUiState.Idle
    }

    fun cancelVotingFlow() {
        _votingContestant.value = null
        _paymentState.value = PaymentUiState.Idle
    }

    // Initialize Mobile Money Transaction
    fun initializePayment(contestant: Contestant, votes: Int, phoneNumber: String, provider: String) {
        viewModelScope.launch {
            val amount = votes * 0.50 // 1 vote = $0.50
            _paymentState.value = PaymentUiState.Processing("Securing channel with $provider...")
            delay(1200) // Realistic loading delay

            val txnId = "TXN-" + UUID.randomUUID().toString().take(8).uppercase()
            _paymentState.value = PaymentUiState.AwaitingPin(
                transactionId = txnId,
                contestant = contestant,
                votesToBuy = votes,
                amount = amount,
                phoneNumber = phoneNumber,
                provider = provider
            )
        }
    }

    // Confirm Pin and Transit to OTP Verification
    fun confirmPin(pin: String) {
        val currentState = _paymentState.value
        if (currentState is PaymentUiState.AwaitingPin) {
            viewModelScope.launch {
                _paymentState.value = PaymentUiState.Processing("Verifying wallet credentials...")
                delay(1500)

                // Generate a simulated OTP
                val generatedOtp = (1000..9999).random().toString()
                _paymentState.value = PaymentUiState.AwaitingOtp(
                    transactionId = currentState.transactionId,
                    contestant = currentState.contestant,
                    votesToBuy = currentState.votesToBuy,
                    amount = currentState.amount,
                    phoneNumber = currentState.phoneNumber,
                    provider = currentState.provider,
                    otpCode = generatedOtp
                )
            }
        }
    }

    // Verify OTP and Complete Payment
    fun verifyOtp(otpInput: String) {
        val currentState = _paymentState.value
        if (currentState is PaymentUiState.AwaitingOtp) {
            viewModelScope.launch {
                if (otpInput == currentState.otpCode) {
                    _paymentState.value = PaymentUiState.Processing("Finalizing instant vote ledger sync...")

                    // Create real database transaction record
                    val transaction = VoteTransaction(
                        transactionId = currentState.transactionId,
                        contestantId = currentState.contestant.id,
                        contestantName = currentState.contestant.name,
                        votesCount = currentState.votesToBuy,
                        amount = currentState.amount,
                        phoneNumber = currentState.phoneNumber,
                        provider = currentState.provider,
                        status = "Pending" // Will be marked completed in repository.completeVote
                    )

                    // Write transaction
                    repository.addTransaction(transaction)
                    delay(1500)

                    // Complete the vote (Room db updates, increments votes)
                    repository.completeVote(
                        txnId = currentState.transactionId,
                        contestantId = currentState.contestant.id,
                        votesCount = currentState.votesToBuy
                    )

                    // Retrieve updated transaction for success display
                    val completedTxn = transaction.copy(status = "Completed")
                    _paymentState.value = PaymentUiState.Success(completedTxn)
                } else {
                    // OTP failed
                    _paymentState.value = currentState.copy(
                        errorMsg = "Invalid verification code. Please check simulated SMS and try again.",
                        otpInput = ""
                    )
                }
            }
        }
    }

    // Cancel Transaction entirely
    fun failOrCancelTransaction() {
        val currentState = _paymentState.value
        viewModelScope.launch {
            if (currentState is PaymentUiState.AwaitingPin) {
                val transaction = VoteTransaction(
                    transactionId = currentState.transactionId,
                    contestantId = currentState.contestant.id,
                    contestantName = currentState.contestant.name,
                    votesCount = currentState.votesToBuy,
                    amount = currentState.amount,
                    phoneNumber = currentState.phoneNumber,
                    provider = currentState.provider,
                    status = "Failed"
                )
                repository.addTransaction(transaction)
            }
            _paymentState.value = PaymentUiState.Idle
            _votingContestant.value = null
        }
    }

    fun dismissSuccess() {
        _paymentState.value = PaymentUiState.Idle
        _votingContestant.value = null
        // Navigate back to Leaderboard to see real-time updates!
        _currentTab.value = PageantTab.LEADERBOARD
    }
}

enum class PageantTab {
    CONTESTANTS,
    LEADERBOARD,
    HISTORY
}

@Suppress("UNCHECKED_CAST")
class PageantViewModelFactory(private val repository: PageantRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PageantViewModel::class.java)) {
            return PageantViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
