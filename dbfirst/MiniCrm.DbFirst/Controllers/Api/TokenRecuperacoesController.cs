using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MiniCrm.DbFirst.Data;
using MiniCrm.DbFirst.Models;

namespace MiniCrm.DbFirst.Controllers.Api
{
    [Route("api/[controller]")]
    [ApiController]
    public class TokenRecuperacoesController : ControllerBase
    {
        private readonly MiniCrmContext _context;

        public TokenRecuperacoesController(MiniCrmContext context)
        {
            _context = context;
        }

        // GET: api/TokenRecuperacoes
        [HttpGet]
        public async Task<ActionResult<IEnumerable<TokenRecuperacao>>> GetTokenRecuperacoes()
        {
            return await _context.TokenRecuperacoes.ToListAsync();
        }

        // GET: api/TokenRecuperacoes/5
        [HttpGet("{id}")]
        public async Task<ActionResult<TokenRecuperacao>> GetTokenRecuperacao(int id)
        {
            var tokenRecuperacao = await _context.TokenRecuperacoes.FindAsync(id);

            if (tokenRecuperacao == null)
            {
                return NotFound();
            }

            return tokenRecuperacao;
        }

        // PUT: api/TokenRecuperacoes/5
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPut("{id}")]
        public async Task<IActionResult> PutTokenRecuperacao(int id, TokenRecuperacao tokenRecuperacao)
        {
            if (id != tokenRecuperacao.IdToken)
            {
                return BadRequest();
            }

            _context.Entry(tokenRecuperacao).State = EntityState.Modified;

            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!TokenRecuperacaoExists(id))
                {
                    return NotFound();
                }
                else
                {
                    throw;
                }
            }

            return NoContent();
        }

        // POST: api/TokenRecuperacoes
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPost]
        public async Task<ActionResult<TokenRecuperacao>> PostTokenRecuperacao(TokenRecuperacao tokenRecuperacao)
        {
            _context.TokenRecuperacoes.Add(tokenRecuperacao);
            await _context.SaveChangesAsync();

            return CreatedAtAction("GetTokenRecuperacao", new { id = tokenRecuperacao.IdToken }, tokenRecuperacao);
        }

        // DELETE: api/TokenRecuperacoes/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteTokenRecuperacao(int id)
        {
            var tokenRecuperacao = await _context.TokenRecuperacoes.FindAsync(id);
            if (tokenRecuperacao == null)
            {
                return NotFound();
            }

            _context.TokenRecuperacoes.Remove(tokenRecuperacao);
            await _context.SaveChangesAsync();

            return NoContent();
        }

        private bool TokenRecuperacaoExists(int id)
        {
            return _context.TokenRecuperacoes.Any(e => e.IdToken == id);
        }
    }
}
